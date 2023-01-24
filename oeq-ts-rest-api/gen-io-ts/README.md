This small project is a code generation project to generate the `io-ts` Codec based on the
`interface`s and `type alias` documented in the main module.

## How it works

1. `ts-morph` is the library used to parse all the TS files in the main module.
2. Based on the parsing output, a library called `io-ts-codegen` is the major tool we use to generate the Codec. However, this library does not generate Codec
for some type definitions. For example, it does not support a type definition that takes type arguments. As a result, we use string templates as a supplementary.

## Example 1
Given a simple type defined as `interface`

```typescript
inteface InterfaceA {
  someString: string;
  someNumber?: number;
  someBoolean?: boolean;
}
```
the output is
```typescript
const InterfaceACodec = t.intersection([
  t.type({
    someString: t.string,
  }),
  t.partial({
    someNumber: t.number,
    someBoolean: t.boolean,
  }),
])
```

## Example 2
Given a simple type defined as `type alias`

```typescript
type TypeA = "a" | "b" | 3;
```
the output is

```typescript
const TypeACodec = t.union([
  t.literal('a'),
  t.literal('b'),
  t.literal(3),
])
```

## Example 3
Given a more complicated type that extends from another type

```typescript
interface InterfaceB extends InterfaceA {
    someArray: string[]
}
```
the output is
```typescript
const InterfaceBCodec = t.intersection([
  t.type({
    someString: t.string,
  }),
  t.partial({
    someNumber: t.number,
    someBoolean: t.boolean,
  }),
  InterfaceACodec
])
```

## TS code requirements
1. If a type definition is for an object, it must be declared by `interface`.
   For example, instead of doing this:
```typescript
 type A = {
   prop: string
 }
```
we should do this:
```
 interface A {
   prop: string;
 }
```
2. When a type definition is imported, the import must be a named type import.
```typescript
 import type { A } from 'X';
```

## Unsupported
1. For `type alias` that accepts type arguments, use interface instead.
2. For `interface` where type arguments and recursive types are both used, while this
   complicated type definition in theory can be supported, we don't have such a type yet and the implementation
   will require a lot more efforts.
