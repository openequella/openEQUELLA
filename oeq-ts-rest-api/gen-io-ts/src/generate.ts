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
import * as E from 'fp-ts/Either';
import { constant, flow, pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import { not } from 'fp-ts/Predicate';
import * as RA from 'fp-ts/ReadonlyArray';
import * as S from 'fp-ts/string';
import type { TypeReference } from 'io-ts-codegen';
import * as gen from 'io-ts-codegen';
import type {
  FileDefinition,
  Import,
  Interface,
  Prop,
  TypeAlias,
} from './parse';
import { pfTernary } from './utils';

const HEADER =
  "/** This file is created by 'io-ts-gen' so please do not modify it. **/";
const IOTS_IMPORT = "import * as t from 'io-ts';";

const ArrayRegex = /^(.+)(\[])+$/;
const RecordRegex = /^Record<(.+), (.+)>$/;
const StringLiteralRegex = /^'(\w*)'$/;
const FunctionRegex = /^\(.*\) => .+$/;
const UnionRegex = /^\|?\s?(.+\n?\s+\|\s)+(.+)$/;
const TupleRegex = /^\[(.+)]$/;

export interface CodecDefinition {
  targetFile: string;
  content: string;
}

// Use this function to build a TypeReference when only the property's type is what you care.
const plainTypeReference = (
  type: string,
  typeArguments?: string[]
): TypeReference =>
  getTypeReference(
    {
      name: '',
      type,
      properties: [],
      optional: false,
    },
    typeArguments
  );

// Given a property and a list of generic types, find out a matched TypeReference for the property.
const getTypeReference = (
  { type, properties }: Prop,
  typeArguments?: string[]
): TypeReference => {
  switch (true) {
    case type === 'boolean':
      return gen.booleanType;
    case type === 'null':
      return gen.nullType;
    case type === 'number':
      return gen.numberType;
    case type === 'object':
      return gen.typeCombinator(generateProperties(properties, typeArguments));
    case type === 'string':
      return gen.stringType;
    case type === 'undefined':
      return gen.undefinedType;
    case type === 'unknown':
      return gen.unknownType;
    case ArrayRegex.test(type):
      return pipe(
        type.match(ArrayRegex)?.[1],
        E.fromNullable(`Failed to extract the array type from ${type} `),
        E.mapLeft(console.error),
        E.foldW(
          () => gen.unknownArrayType,
          (t) => gen.arrayCombinator(plainTypeReference(t, typeArguments))
        )
      );
    case FunctionRegex.test(type):
      return gen.functionType;
    case RecordRegex.test(type):
      return pipe(
        type.match(RecordRegex),
        E.fromNullable(`Failed to extract the generic types from ${type}`),
        E.mapLeft(console.error),
        E.foldW(
          () => gen.unknownRecordType,
          ([_, domain, codomain]) =>
            gen.recordCombinator(
              plainTypeReference(domain, typeArguments),
              plainTypeReference(codomain, typeArguments)
            )
        )
      );
    case StringLiteralRegex.test(type):
      return pipe(
        type.match(StringLiteralRegex)?.[1],
        E.fromNullable(`Failed to extract string literal from ${type}`),
        E.mapLeft(console.error),
        E.foldW(() => gen.unknownType, gen.literalCombinator)
      );
    case UnionRegex.test(type):
      return pipe(
        type.match(UnionRegex)?.input,
        E.fromNullable(`Failed to extract string union from ${type}`),
        E.mapLeft(console.error),
        E.map(
          flow(
            S.split('|'),
            RA.map(S.trim),
            RA.filter(not(S.isEmpty)),
            RA.map(plainTypeReference),
            RA.toArray
          )
        ),
        E.foldW(() => gen.unknownType, gen.unionCombinator)
      );
    case TupleRegex.test(type):
      return pipe(
        type.match(TupleRegex)?.[1],
        E.fromNullable(`Failed to extract types from tuple ${type}`),
        E.map(
          flow(
            S.split(','),
            RA.map(S.trim),
            RA.map(plainTypeReference),
            RA.toArray
          )
        ),
        E.mapLeft(console.error),
        E.foldW(
          () => gen.unknownType,
          (tuple) => gen.tupleCombinator(tuple)
        )
      );
    default:
      return pipe(
        typeArguments,
        O.fromNullable,
        O.chain(A.findIndex((arg) => arg === type)),
        O.getOrElse(constant(-1)),
        (index) => (index >= 0 ? `codec${index}` : `${type}Codec`),
        gen.identifier
      );
  }
};

// Return a list of io-ts-codegen Property for the supplied list of Prop.
const generateProperties = (
  props: Prop[],
  typeArguments?: string[]
): gen.Property[] =>
  pipe(
    props,
    A.map((p) =>
      gen.property(p.name, getTypeReference(p, typeArguments), p.optional)
    )
  );

// Return a list of io-ts-codegen Identifier for the supplied list of Interface's name.
const generateExtendIdentifier: (identifiers: string[]) => gen.Identifier[] =
  flow(A.map((identifier) => gen.identifier(`${identifier}Codec`)));

// Generate all the named imports for required Codecs.
const generateImports: (imports: Import[]) => string = flow(
  A.map(
    ({ filename, namedImport }) =>
      `import { ${namedImport}Codec } from '${filename}'`
  ),
  A.intercalate(S.Monoid)('\n')
);

// Use io-ts-codegen to build an interfact that does not take type arguments.
const buildNormalInterface = ({
  name,
  properties,
  typeExtended,
}: Interface) => {
  const extendIdentifiers = generateExtendIdentifier(typeExtended);
  const props = gen.typeCombinator(generateProperties(properties));

  const typeDeclaration = gen.typeDeclaration(
    `${name}Codec`,
    A.isNonEmpty(extendIdentifiers)
      ? gen.intersectionCombinator([props, ...extendIdentifiers])
      : props,
    true
  );

  return gen.printRuntime(typeDeclaration);
};

// is-ts-codegen does not have any support out-of-box for interfaces that takes type arguments.
// A codec for such a function is represented by a function. As a result, we use a string template
// to generate the function.
const buildGenericTypeInterface = ({
  name,
  properties,
  typeArguments,
  typeExtended,
}: Interface): string => {
  const typeParameters = typeArguments
    .map((_, index) => `C${index} extends t.Mixed`)
    .join(',');
  const funcParameters = typeArguments
    .map((_, index) => `codec${index}: C${index}`)
    .join(',');
  const extendIdentifiers = generateExtendIdentifier(typeExtended);
  const props = gen.typeCombinator(
    generateProperties(properties, typeArguments)
  );

  const body = gen.printRuntime(
    A.isNonEmpty(extendIdentifiers)
      ? gen.intersectionCombinator([props, ...extendIdentifiers])
      : props
  );

  return `const ${name}Codec = <${typeParameters}>(${funcParameters}) => {
           ${body}
     }`;
};

const buildCodecForInterface: (interfaceDefinition: Interface) => string = flow(
  pfTernary(
    ({ typeArguments }) => A.size(typeArguments) > 0,
    buildGenericTypeInterface,
    buildNormalInterface
  )
);

const buildCodecForTypeAlias = ({ name, referencedType }: TypeAlias): string =>
  gen.printRuntime(
    gen.typeDeclaration(
      `${name}Codec`,
      plainTypeReference(referencedType),
      true
    )
  );

export const generate = ({
  filename,
  imports,
  interfaces,
  typeAliases,
}: FileDefinition): CodecDefinition =>
  pipe(
    typeAliases,
    A.map(buildCodecForTypeAlias),
    A.concat(interfaces.map(buildCodecForInterface)),
    (typeDeclarations) => [
      HEADER,
      IOTS_IMPORT,
      generateImports(imports),
      ...typeDeclarations,
    ],
    A.intercalate(S.Monoid)('\n'),
    (content) => ({ targetFile: filename, content })
  );
