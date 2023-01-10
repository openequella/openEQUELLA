This small project is a code generation project to generate the `io-ts` types based on the
`interface`s documented in the main module.

## Output Format

Given the input a directory with two files. First `File1.ts`:

```typescript
inteface InterfaceA extends InterfaceX, InterfaceY {
  someString: string;
  someNumber?: number;
  someBoolean?: boolean;
}

inteface InterfaceB {
  someNumber: number;
  anArrayOfA: InterfaceA[];
}
```

Then `File2.ts`:

```typescript
import InterfaceA from File1;

interface InterfaceC {
  someNumber: number;
  someString? string;
  someObject: {
    anA: IntefaceA;
    someBoolean?: boolean;
  }
```
This would produce the YAML of:

```yaml
files:
  - File1.ts
    interfaces:
      - InterfaceA:
        extends:
          - Interface X
          - Interface Y
        properties:
          someString:
            type: string
          someNumber:
            type: number
            optional: true
          someBoolean:
            type: boolean
            optional: true
      - InterfaceB:
        properties:
          someNumber:
            type: number
          anArrayOfA:
            type: array(InterfaceA)
  - File2.ts
    imports:
      File1:
        - InterfaceA
    interfaces:
      - InterfaceC
        properties:
          someNumber:
            type: number
          someString:
            type: string
            optional: true
          someObject:
            type: inline-object
            object-properties:
              anA:
                type: InterfaceA
              someBoolean:
                type: boolean
                optional: true
```

## Resources

Project setup was based on: [Building a Typescript CLI](https://dev.to/akshaynathan/building-a-typescript-cli-26h5)
