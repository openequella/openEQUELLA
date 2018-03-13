const spawn = require('cross-spawn');
const CombinedStream = require('combined-stream2');

const main = process.argv[2];
const outjs = "target/web/reactjs/"+process.argv[3];
console.log("Building production bundle: "+outjs);
const res = spawn("pulp", ["build", "--skip-entry-point", "-O", "-m", main], { stdio: [process.stdin, 'pipe', process.stderr]})

var jsbundle = CombinedStream.create()
jsbundle.append(res.stdout);
jsbundle.append(Buffer.from("module.exports = PS[\""+main+"\"];\n"));

var browserify = spawn("browserify", ["--standalone", "PS", "-g", "[", "envify", "--NODE_ENV", "production", "]", 
    "-g", "uglifyify", "-"], { stdio: ['pipe', 'pipe', 'inherit'] });
jsbundle.pipe(browserify.stdin);

var uglilfy = spawn("uglifyjs", ["-o", outjs, "--compress", "--mangle"], { stdio: ['pipe', 'inherit', 'inherit'] });
browserify.stdout.pipe(uglilfy.stdin);
