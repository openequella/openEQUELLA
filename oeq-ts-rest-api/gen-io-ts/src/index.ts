/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as A from 'fp-ts/Array';
import { pipe } from 'fp-ts/function';
import * as fs from 'fs';
import * as path from 'path';
import yargs from 'yargs';
import { parseFile } from './parse';

const args = yargs
  .options({
    dest: {
      type: 'string',
      demandOption: true,
      describe: 'Folder containing typescript files to parse',
    },
  })
  .parseSync();

console.log(`Processing directory: ${args.dest}`);
pipe(
  fs.readdirSync(args.dest),
  A.map((filename) => path.resolve(args.dest, filename)),
  A.filter((filename) => !fs.lstatSync(filename).isDirectory()),
  // A.map(parseFile),
  A.map(console.log)
);

// const program = pipe(
pipe(
  // '/home/penghai/Edalex/gitlab/fork/openequella/oeq-ts-rest-api/src/Schema.ts',
  '/home/penghai/Edalex/gitlab/fork/openequella/oeq-ts-rest-api/src/Settings.ts',
  parseFile,
  (output) => console.dir(output, { depth: null })
  // TE.fold(C.error, C.log)
);

// const processingFailure = (msg: string) =>
//   console.error(`Processing failed: ${msg}`);
//
// program()
//   .then(
//     flow(
//       E.match(processingFailure, (output) => {
//         console.dir(output, { depth: null });
//         console.log('Processing completed successfully.');
//       })
//     )
//   )
//   .catch(processingFailure);
