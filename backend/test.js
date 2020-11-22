const bcrypt = require('bcrypt');


const r = bcrypt.compareSync('matine74', '$2y$10$KSHXZFmCFxGTSWTge2urPuyRGyMAJjaXJ.6p1QiDzMO8oGmDMH4Qm');
var hash = bcrypt.hashSync("lol", 10);
console.log(r, hash);
