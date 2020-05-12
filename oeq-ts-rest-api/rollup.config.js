import commonjs from '@rollup/plugin-commonjs';
import json from '@rollup/plugin-json';
import resolve from '@rollup/plugin-node-resolve';
import ttypescript from 'ttypescript';
import typescript from '@rollup/plugin-typescript';

export default {
  input: 'src/index.ts',
  output: {
    dir: 'dist',
    format: 'es',
    sourcemap: true,
  },
  plugins: [
    commonjs({ include: 'node_modules/**' }),
    json(),
    resolve({ preferBuiltins: false, modulesOnly: true }),
    typescript({ typescript: ttypescript }), // so Rollup can convert TypeScript to JavaScript
  ],
};
