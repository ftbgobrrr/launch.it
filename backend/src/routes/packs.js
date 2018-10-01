import express from 'express';
import jwt from 'jwt-express';
import { ObjectId } from 'mongodb';
import {
    mojang, appRoot, HOST, PORT,
} from '..';
import fse from 'fs-extra';
import { promisify } from 'util';
import sha1FileCallback from 'sha1-file';
import { groupToLevel, EDITOR, path as nameToPath } from '../utils';
import { error, INVALID_RESULT, INCOMPATIBLE_VERSION } from '../errors';

const sha1File = promisify(sha1FileCallback);
const router = express.Router();

router.use(jwt.active());

router.get('/', async (req, res) => {
    const packs = await req.db
        .collection('packs')
        .find({})
        .map(({ _id, ...fields }) => ({ id: _id, ...fields }))
        .toArray();
    res.status(200);
    res.json(packs);
});

router.post('/add', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { name, preset } = req.body;
    const { data: version } = await mojang.version(preset, true);
    console.log(version);
    req.db.collection('packs')
        .insertOne({ name, preset })
        .then(({ insertedId }) => {
            res.status(200).json({ id: insertedId, name, preset });
        });
});

router.post('/del', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { id } = req.body;
    const { deletedCount } = await req.db.collection('packs')
        .deleteOne({ _id: new ObjectId(id) });
    if (deletedCount == 0) return next();
    res.status(200).json({ id });
}, ({ res }) => error(res, INVALID_RESULT));

router.post('/edit', jwt.require('level', '>=', groupToLevel(EDITOR)), async (req, res) => {
    const { id, name } = req.body;
    const pack = { name };
    const { modifiedCount } = await req.db.collection('packs')
        .updateOne(
            { _id: new ObjectId(id) },
            { $set: pack },
        );
    if (modifiedCount == 0) return next();
    res.status(200).json({ id });
}, ({ res }) => error(res, INVALID_RESULT));

router.post('/pack', async (req, res) => {
    const { id } = req.body;
    const pack = await req.db
        .collection('packs')
        .find({ _id: new ObjectId(id) })
        .map(({ _id, ...fields }) => ({ id: _id, ...fields }))
        .next();
    res.status(200);
    try {
        const { data } = await mojang.version(pack.preset);
        if (pack && pack.libraries) {
            data.libraries = [...pack.libraries, ...data.libraries];
        }
        res.json({ ...pack, data });
    } catch (err) {
        error(res, INCOMPATIBLE_VERSION);
    }
});

const plurializeTypes = (type) => {
    if (type === 'library') {
        return 'libraries';
    } if (type === 'file') {
        return 'files';
    }
    return undefined;
};

router.post('/pack/upload', async (req, res, next) => {
    // pack id de la ressource
    // type = type librayr/file
    // name = truc formatter
    const { pack, type, name } = req.body;
    const plurializedType = plurializeTypes(type);
    if (!plurializedType) {
        res.end();
        return;
    }
    try {
        const { files: { file } } = req;
        const path = nameToPath(name);
        const out = `${appRoot}/public/${plurializedType}/${path}`;
        await fse.createFile(out);
        await file.mv(out);
        const url = `http://${HOST}:${PORT}/public/${plurializedType}/${path}`;
        const { size } = await fse.stat(out);
        const sha1 = await sha1File(out);

        const lib = {
            name,
            downloads: {
                artifact: {
                    path,
                    url,
                    size,
                    sha1,
                },
            },
        };

        const { modifiedCount } = await req.db.collection('packs')
            .updateOne(
                { _id: new ObjectId(pack) },
                { $push: { [plurializedType]: lib } },
            );
        if (modifiedCount === 0) {
            next();
            return;
        }
        console.log('ok');
        res.json(lib);
    } catch (err) {
        console.error(err);
        next();
    }
}, ({ res }) => error(res, INVALID_RESULT));

module.exports = router;
