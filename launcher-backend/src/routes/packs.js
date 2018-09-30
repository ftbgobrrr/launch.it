import express from 'express'
import { error, INVALID_RESULT, INCOMPATIBLE_VERSION } from '../errors'
import jwt from 'jwt-express'
import { ObjectId } from 'mongodb'
import { groupToLevel, EDITOR, path as nameToPath } from '../utils'
import { mojang, appRoot, HOST, PORT } from '../main'
import fse from 'fs-extra'
import { promisify } from 'util'
import sha1FileCallback from 'sha1-file';

const sha1File = promisify(sha1FileCallback);
const router = express.Router()

router.use(jwt.active())

router.get('/', async (req, res) => {
    const packs = await req.db
        .collection('packs')
        .find({})
        .map(({ _id, ...fields }) => ({ id: _id, ...fields }))
        .toArray()
    res.status(200)
    res.json(packs)
})

router.post('/add', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { name, preset } = req.body
    const { data: version } = await mojang.version(preset, true)
    console.log(version)
    req.db.collection('packs')
        .insertOne({ name, preset })
        .then(({ insertedId }) => {
            res.status(200).json({ id: insertedId, name, preset })
        })
})

router.post('/del', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { id } = req.body
    const { deletedCount } = await req.db.collection('packs')
        .deleteOne({ _id: new ObjectId(id) })
    if (deletedCount == 0)
        return next()
    res.status(200).json({ id })
}, ({ res }) => error(res, INVALID_RESULT))

router.post('/edit', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { id, name } = req.body;
    const pack = { name };
    const { modifiedCount } = await req.db.collection('packs')
        .updateOne(
            { _id: new ObjectId(id) },
            { $set: pack }
        )
    if (modifiedCount == 0)
        return next()
    res.status(200).json({ id });
}, ({ res }) => error(res, INVALID_RESULT));

router.post('/pack', async (req, res) => {
    const { id } = req.body;
    const pack = await req.db
        .collection('packs')
        .find({ _id: new ObjectId(id) })
        .map(({ _id, ...fields }) => ({ id: _id, ...fields }))
        .next()
    res.status(200)
    try {
        const { data } = await mojang.version(pack.preset);
        res.json({ ...pack, data })
    } catch (err) {
        error(res, INCOMPATIBLE_VERSION)
        return;
    }
})

router.post('/pack/upload', async (req, res, next) => {
    const { pack, type, name } = req.body;

    try {
        const file = req.files.file;
        const path = nameToPath(name);
        const out = `${appRoot}/public/libraries/${path}`
        await fse.createFile(out)
        await file.mv(out)
        const url = `http://${HOST}:${PORT}/public/libraries/${path}`
        const { size } = await fse.stat(out)
        const sha1 = await sha1File(out)

        const lib = { 
            name,
            downloads: {
                artifact: {
                    path,
                    url,
                    size,
                    sha1
                }
            }
        }

        const { modifiedCount } = await req.db.collection('packs')
            .updateOne(
                { _id: new ObjectId(pack) },
                { $push: { libraries: lib } }
            )
        if (modifiedCount == 0)
            return next()
        res.json(lib);
    } catch (err) {
        console.error(err);
        return next()
    }

}, ({ res }) => error(res, INVALID_RESULT));

module.exports = router;