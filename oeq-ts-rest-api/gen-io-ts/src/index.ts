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
import { constVoid, flow, pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import * as fs from 'fs';
import * as path from 'path';
import yargs from 'yargs';
import { CodecDefinition, generate } from './generate';
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

const outputPath = `${path.resolve(__dirname)}/../../gen`;

const writeFile = ({ targetFile, content }: CodecDefinition): void => {
  console.log(`Writing codec content to ${targetFile}...`);
  fs.writeFile(
    `${outputPath}/${targetFile}`,
    content,
    flow(O.fromNullable, O.fold(constVoid, console.error))
  );
};

if (!fs.existsSync(outputPath)) {
  fs.mkdirSync(outputPath);
}

console.log(`Processing directory: ${args.dest}`);

pipe(
  fs.readdirSync(args.dest),
  A.map((filename) => path.resolve(args.dest, filename)),
  A.filter((filename) => !fs.lstatSync(filename).isDirectory()),
  A.map(
    flow(
      parseFile,
      // Filer out files that neither have interfaces nor type alias.
      O.fromPredicate(
        ({ interfaces, typeAliases }) =>
          A.isNonEmpty(interfaces) || A.isNonEmpty(typeAliases)
      ),
      O.fold(constVoid, flow(generate, writeFile))
    )
  )
);
