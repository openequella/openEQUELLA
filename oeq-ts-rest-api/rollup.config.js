// import builtins from 'rollup-plugin-node-builtins';
import commonjs from '@rollup/plugin-commonjs';
// import globals from 'rollup-plugin-node-globals';
import json from '@rollup/plugin-json';
import resolve from '@rollup/plugin-node-resolve';
import typescript from '@rollup/plugin-typescript';

export default {
  input: 'src/index.ts',
  output: {
    dir: 'dist',
    format: 'es',
    sourcemap: true,
  },
  plugins: [
    // builtins(),
    commonjs( {include: 'node_modules/**' }),
    // globals(),
    json(),
    resolve({ preferBuiltins: false, modulesOnly: true }),
    typescript(), // so Rollup can convert TypeScript to JavaScript
  ],
};
