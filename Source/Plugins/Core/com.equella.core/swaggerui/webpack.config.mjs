import webpack from 'webpack';
import path from "path";

const config = (env) => {
  const production = !env.WEBPACK_WATCH;
  return ({
    entry: {
      app: "./index.js"
    },
    resolve: {
      extensions: [".js"],
    },
    plugins: [
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify(production ? 'production' : 'development')
      })
    ],
    output: {
      filename: "bundle.js",
      path: path.resolve(production ? "target/" : "../target/scala-2.13/classes/web/apidocs/"),
    },
  });
}

export default config;
