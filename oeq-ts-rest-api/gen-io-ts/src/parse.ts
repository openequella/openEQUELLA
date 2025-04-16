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
import { flow, pipe } from 'fp-ts/function';
import {
  ImportDeclaration,
  InterfaceDeclaration,
  Project,
  PropertySignature,
  SourceFile,
  SyntaxKind,
  TypeAliasDeclaration,
  TypeFormatFlags,
} from 'ts-morph';
import { pfTernary } from './utils';

export interface Import {
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

export interface Prop {
  /**
   * Name of the property.
   */
  name: string;
  /**
   * Type of the property.
   */
  type: string;
  /**
   * `true` if the property is optional.
   */
  optional: boolean;
  /**
   * For properties which are inline objects, then they'll have further properties of their own.
   */
  properties: Prop[];
}

export interface Interface {
  /**
   * Name of the interface.
   */
  name: string;
  /**
   * Interfaces that are extended by this interface.
   */
  typeExtended: string[];
  /**
   * Properties belonging to this interface.
   */
  properties: Prop[];
  /**
   * A list of type arguments required by the interface.
   */
  typeArguments: string[];
}

export interface TypeAlias {
  /**
   * Alias for the referenced type.
   */
  name: string;
  /**
   * The original type referenced by the alias.
   */
  referencedType: string;
}

/**
 * Contains the definitions of interest for a file.
 */
export interface FileDefinition {
  /**
   * Name of the Typescript file to be parsed.
   */
  readonly filename: string;
  /**
   * A list of imports required in this file.
   */
  readonly imports: Import[];
  /**
   * A list of Interface defined in this file.
   */
  readonly interfaces: Interface[];
  /**
   * A list of type alias defined in this file.
   */
  readonly typeAliases: TypeAlias[];
}

// Return names of interfaces extended by the supplied interface.
const buildExtends = (i: InterfaceDeclaration): string[] =>
  i.getExtends().map((e) => e.getText());

const buildProperties = (props: PropertySignature[]): Prop[] => {
  const getPropertyType = (p: PropertySignature): string =>
    p.getTypeNode()?.getText() ??
    p.getType().getText(undefined, TypeFormatFlags.None);

  const commonFields = (p: PropertySignature) => ({
    name: p.getName(),
    optional: p.hasQuestionToken(),
  });

  const buildNonObjectProperty = (p: PropertySignature) => ({
    ...commonFields(p),
    type: getPropertyType(p),
    properties: [], // not a nested object, so always empty
  });

  const buildObjectProperty = (p: PropertySignature): Prop => {
    const isFunc = p.getTypeNode()?.isKind(SyntaxKind.FunctionType);
    const isAnonymous = !isFunc && p.getType().isAnonymous();
    return {
      ...buildNonObjectProperty(p),
      type: isAnonymous ? 'object' : getPropertyType(p), // For non-anonymous object we need the real name.
      properties: isAnonymous
        ? pipe(
            p.getType().getProperties(),
            A.chain((p) => p.getDeclarations() as PropertySignature[]),
            buildProperties
          )
        : [],
    };
  };

  return props.map(
    pfTernary(
      (p) => p.getType().isObject(),
      buildObjectProperty,
      buildNonObjectProperty
    )
  );
};

const buildInterfaces: (interfaces: InterfaceDeclaration[]) => Interface[] =
  flow(
    A.map((i) => ({
      name: i.getName(),
      typeExtended: buildExtends(i),
      properties: buildProperties(i.getProperties()),
      typeArguments: i
        .getType()
        .getTypeArguments()
        .map((t) => t.getText(undefined, TypeFormatFlags.None)),
    }))
  );

const buildImports = (declarations: ImportDeclaration[]): Import[] =>
  pipe(
    declarations,
    A.filter((d) => d.isTypeOnly()),
    A.chain((d) =>
      pipe(
        d.getNamedImports(),
        A.map((i) => ({
          filename: d.getModuleSpecifierValue(),
          namedImport: i.getName(),
        }))
      )
    )
  );

const buildTypeAliases: (typeAliases: TypeAliasDeclaration[]) => TypeAlias[] =
  flow(
    A.map((alias) => ({
      name: alias.getName(),
      referencedType:
        alias.getTypeNode()?.getText() ?? alias.getType().getText(),
    }))
  );

const buildFileDefinition = (file: SourceFile): FileDefinition => ({
  filename: file.getBaseName(),
  interfaces: buildInterfaces(file.getInterfaces()),
  imports: buildImports(file.getImportDeclarations()),
  typeAliases: buildTypeAliases(file.getTypeAliases()),
});

/**
 * This function parses a TS file and builds a FileDefinition for the file, which includes:
 * 1. All the interface definitions.
 * 2. All the type alias definitions.
 * 3. All the imports required by above interfaces or type alias.
 *
 * @param path Absolute path to a TS file.
 */
export const parseFile = (path: string): FileDefinition =>
  pipe(new Project(), (p) => p.addSourceFileAtPath(path), buildFileDefinition);
