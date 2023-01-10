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
import { constant, pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import * as S from 'fp-ts/string';
import {
  ImportDeclaration,
  InterfaceDeclaration,
  Project,
  PropertySignature,
  SourceFile,
} from 'ts-morph';
import { pfTernary } from './utils';

interface Import {
  /**
   * Name of file which the imports are from. e.g.
   *
   * ```
   * import Xyz from '<filename>';
   * ```
   */
  filename: string;
  /**
   * What is imported from this file.
   *
   * For example,
   * ```
   * import { A } from 'somefile';
   * ```
   * The value is A.
   *
   * Multiple named imports from the same file will be converted into multiple `Import`.
   */
  namedImport: string;
}

interface Prop {
  name: string;
  type: string;
  optional: boolean;
  /**
   * For properties which are inline objects, then they'll have further properties of their own.
   */
  properties: Prop[];
}

interface Interface {
  name: string;
  extends: string[];
  properties: Prop[];
}

/**
 * Contains the definitions of interest for a file.
 */
interface FileDefinition {
  readonly imports: Import[];
  readonly interfaces: Interface[];
}

const buildExtends = (i: InterfaceDeclaration): string[] =>
  pipe(
    // The recommended approach is i.getExtends().map(e => e.getTypeArguments().map(...))
    // But the getTypeArguments was empty for me.
    // So I came up with this after using the TypeScript AST Viewer - https://ts-ast-viewer.com/
    i.getHeritageClauses(),
    A.chain((hc) =>
      pipe(
        hc.getTypeNodes(),
        A.map((node) => node.getText().replace(/<.+>/, ''))
      )
    )
  );

const buildProperties = (props: PropertySignature[]): Prop[] => {
  const isInlineObject = (p: PropertySignature): boolean =>
    p.getType().isAnonymous() && p.getType().isObject();

  const buildObjectProperty = (p: PropertySignature): Prop => ({
    name: p.getName(),
    type: 'object',
    optional: p.hasQuestionToken(),
    properties: pipe(
      p.getType().getProperties(),
      A.chain((p) => p.getDeclarations() as PropertySignature[]),
      buildProperties
    ),
  });

  // It is possible to get the type via a simple:
  //   p.getType().getText()
  // But that didn't always give a simple name, so we've gone with
  // the below slightly more convoluted approach.
  const getPropertyType = (p: PropertySignature): string =>
    pipe(
      p.getStructure().type,
      O.fromNullable,
      O.filter(S.isString),
      O.getOrElse(constant('unknown'))
    );

  return pipe(
    props,
    A.map(
      pfTernary(isInlineObject, buildObjectProperty, (p) => ({
        name: p.getName(),
        type: getPropertyType(p),
        optional: p.hasQuestionToken(),
        properties: [], // not a nested object, so always empty
      }))
    )
  );
};

const buildInterfaces = (interfaces: InterfaceDeclaration[]): Interface[] =>
  pipe(
    interfaces,
    A.map((i: InterfaceDeclaration) => ({
      name: i.getName(),
      extends: buildExtends(i),
      properties: buildProperties(i.getProperties()),
    }))
  );

const buildImports = (
  interfaceExtended: string[],
  parsedImports: ImportDeclaration[]
): Import[] => {
  const interfaceExtendedFromImport = ({
    namedImport,
  }: {
    namedImport: string;
  }) =>
    pipe(
      interfaceExtended,
      A.some((i) => i === namedImport)
    );

  console.log(interfaceExtended);
  const a = pipe(
    parsedImports,
    A.chain((i) => i.getNamedImports()),
    A.map((namedImport) => {
      const moduleSpecifier = namedImport
        .getParent()
        .getParent()
        .getParent()
        .getModuleSpecifierValue();
      return {
        filename: moduleSpecifier,
        namedImport: namedImport.getName(),
      };
    }),
    A.filter(interfaceExtendedFromImport),
    A.map(({ filename, namedImport }) => ({
      filename,
      namedImport,
    }))
  );

  console.log(a);
  return a;
};

const buildFileDefinition = (file: SourceFile): FileDefinition =>
  pipe(buildInterfaces(file.getInterfaces()), (interfaces) => ({
    interfaces,
    imports: buildImports(
      pipe(
        interfaces,
        A.chain((i) => i.extends),
        A.uniq(S.Eq)
      ),
      file.getImportDeclarations()
    ),
  }));

export const parseFile = (filename: string) =>
  pipe(
    new Project(),
    (p) => p.addSourceFileAtPath(filename),
    buildFileDefinition
  );
