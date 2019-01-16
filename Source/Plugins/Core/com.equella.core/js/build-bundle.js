const spawn = require("cross-spawn");
const fs = require("fs");
const ArgumentParser = require("argparse").ArgumentParser;
const mkdirp = require("mkdirp");
const classesDir = "../target/scala-2.12/classes/web/reactjs/";
const targetDir = "target/resources/web/reactjs/";
const psTargetDir = "target/ps/";
const path = require("path");

var env = Object.create(process.env);

var parser = new ArgumentParser({
  version: "1.0.0",
  addHelp: true,
  description: "Build PS bundles"
});
parser.addArgument("module", { help: "Module name" });
parser.addArgument("out", { help: "Javascript filename" });
parser.addArgument("--dev", { help: "Build dev bundle", action: "storeTrue" });
parser.addArgument("--lib", { help: "Build as library", action: "storeTrue" });
parser.addArgument("--devpath", {
  help: "Build production bundle to dev path",
  action: "storeTrue"
});

const args = parser.parseArgs();
const main = args.module;

function buildBundle(bundle, devpath, dev) {
  var destDir = targetDir;
  if (devpath || dev) {
    destDir = classesDir;
  }
  mkdirp.sync(destDir);
  mkdirp.sync(psTargetDir);
  const outjs = destDir + bundle;
  const psBundle = psTargetDir + bundle;
  console.log("Building " + (dev ? "dev" : "production") + " bundle: " + outjs);

  if (dev) {
    var pargs = [
      "--watch",
      "--before",
      "sleep 3 && clear",
      "browserify",
      "-I",
      "target/ts",
      "--to",
      outjs,
      "-m",
      main
    ];
    if (args.lib) {
      pargs.push("--no-check-main", "--standalone", "PS");
    }
    env.NODE_PATH = "./target/ts";
    spawn.sync("pulp", pargs, { env: env, stdio: "inherit" });
  } else {
    env.NODE_PATH = "./target/ts";

    var pargs = ["build", "--to", psBundle, "--skip-entry-point", "-m", main];

    var bargs = [
      "-g",
      "[",
      "envify",
      "--NODE_ENV",
      "production",
      "]",
      "-g",
      "uglifyify",
      psBundle,
      "-"
    ];

    const pulp = spawn.sync("pulp", pargs, { stdio: "inherit" });
    var psExport = args.lib ? "['" + main + "']" : "";
    fs.appendFileSync(psBundle, "module.exports = PS" + psExport + ";");

    var mainModule = 'require("./' + psBundle + '")';
    var booter;
    if (args.lib) {
      bargs.push("--standalone", "PS");
      booter = mainModule + ";";
    } else {
      booter = mainModule + '["' + main + '"].main();';
    }
    console.log("* Browserify+uglify-ing bundle");
    var browserify = spawn("browserify", bargs, {
      env: env,
      stdio: ["pipe", "pipe", "inherit"]
    });
    browserify.stdin.write(booter);
    browserify.stdin.end();
    var uglify = spawn("uglifyjs", ["-o", outjs, "--compress", "--mangle"], {
      stdio: ["pipe", "inherit", "inherit"]
    });
    browserify.stdout.pipe(uglify.stdin);
    browserify.on("exit", function(code) {
      if (code != 0) {
        process.exit(code);
      }
      uglify.on("exit", function(code) {
        process.exit(code);
      });
    });
  }
}

buildBundle(args.out, args.devpath, args.dev);
