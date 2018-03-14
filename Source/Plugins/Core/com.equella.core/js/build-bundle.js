const spawn = require('cross-spawn');
const CombinedStream = require('combined-stream2');
const ArgumentParser = require("argparse").ArgumentParser;
const mkdirp = require('mkdirp');
const classesDir = "../target/scala-2.12/classes/web/reactjs/";
const targetDir = "target/resources/web/reactjs/"

var parser = new ArgumentParser({
    version: '1.0.0',
    addHelp:true,
    description: 'Build PS bundles'
  });
parser.addArgument('module',{ help: 'Module name' } );
parser.addArgument('out', { help: 'Javascript filename' });
parser.addArgument('--dev', { help: 'Build dev bundle', action:'storeTrue' });
parser.addArgument('--lib', { help: 'Build as library', action:'storeTrue' });
parser.addArgument('--devpath', { help: 'Build production bundle to dev path', action:'storeTrue' });

const args =  parser.parseArgs();
const main = args.module;

function buildDev()
{
    const outjs = classesDir + args.out;
    mkdirp.sync(classesDir);
    console.log("Starting dev mode for: "+args.out);
    var pargs = ["-w", "browserify", "-m", main, "--to", outjs]
    if (args.lib) {
        pargs.push("--no-check-main", "--standalone", "PS");
    }
    spawn.sync("pulp", pargs, {stdio:'inherit'});
}

function buildProd()
{
    var destDir = targetDir;
    if (args.devpath) {
        destDir = classesDir;
    }
    mkdirp.sync(destDir);
    const outjs = destDir+args.out;
    console.log("Building production bundle: "+outjs);
    var pargs = ["build", "-O", "-m", main]
    var bargs = []
    if (args.lib)
    {
        pargs.push("--skip-entry-point"); 
        bargs.push("--standalone", "PS");
    }
    bargs.push("-g", "[", "envify", "--NODE_ENV", "production", "]", 
    "-g", "uglifyify", "-");
    const pulp = spawn("pulp", pargs, { stdio: [process.stdin, 'pipe', process.stderr]})

    var jsbundle = CombinedStream.create()
    jsbundle.append(pulp.stdout);
    jsbundle.append(Buffer.from("module.exports = PS[\""+main+"\"];\n"));

    var browserify = spawn("browserify", bargs, { stdio: ['pipe', 'pipe', 'inherit'] });
    jsbundle.pipe(browserify.stdin);

    var uglilfy = spawn("uglifyjs", ["-o", outjs, "--compress", "--mangle"], { stdio: ['pipe', 'inherit', 'inherit'] });
    browserify.stdout.pipe(uglilfy.stdin);
}

if (args.dev) buildDev(); 
else buildProd();
