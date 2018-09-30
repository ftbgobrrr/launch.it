import express from 'express'
import { error, INVALID_RESULT } from '../errors'
import jwt from 'jwt-express'
import { ObjectId } from 'mongodb'
import { groupToLevel, VERSIONS_URL } from '../utils'
import { mojang } from '../main';

const router = express.Router();

router.use(jwt.active())

router.get('/', async (req, res) => {
    const versions = await mojang.versions();
    res.json(versions).status(200);
});

module.exports = router;