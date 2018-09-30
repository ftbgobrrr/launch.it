import express from 'express'
import { addRoutes } from './routes'
import mongo from 'express-mongo-db'
import bodyParser from 'body-parser'
import cookieParser from 'cookie-parser'
import jwt from 'jwt-express'
import upash from 'upash'
import cors from 'cors'
import logger from 'morgan'
import fileUpload from 'express-fileupload'
import { error, INVALID_TOKEN, INSUFFICIENT_PERMISSION } from './errors'
import Mojang from './mojang'

const app = express()
const mojang = new Mojang();
const {
    PORT = 3000,
    HOST = `localhost`,
    AUTH_SECRET = 'ducon'
} = process.env

app.use(logger('dev'));

app.use(mongo('mongodb://localhost/launcher'))
upash.install('argon2', require('@phc/argon2'))
app.use(cors({
    origin: true,
    credentials: true,
    optionsSuccessStatus: 200
}))
app.use(cookieParser())
app.use(jwt.init(AUTH_SECRET, { cookie: 'auth' }))
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: true }))
app.use(fileUpload())
app.use('/public', express.static(__dirname + '/public'));

addRoutes(app)

app.use(function(err, req, res, next) {
    if (err.name == 'JWTExpressError') {
        if (err.message == 'JWT is insufficient')
            error(res, INSUFFICIENT_PERMISSION);
        else
            error(res, INVALID_TOKEN);
    } else {
        next(err);
    }
});

app.listen(PORT, () => {
    console.log(`Backend bind on port ${PORT}`)
})

const appRoot = __dirname;

export {
    mojang,
    appRoot,
    HOST,
    PORT
}