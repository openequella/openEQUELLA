import * as Lodash from "lodash";

/**
 * Performs inplace conversion of specified fields with supplied converter.
 *
 * @param input The object to be processed.
 * @param targetFields List of the names of fields to convert.
 * @param recursive True if processing nested objects is required.
 * @param converter A function converting fields' type.
 */
const convertFields = <T, R>(input: unknown, targetFields: string[], recursive: boolean, converter: (value: T) => R): void => {
  const entries: [string, any][] = Object.entries(input as any);

  entries.forEach(([field, value]) => {
    if(typeof value === "object" && recursive) {
      convertFields(value, targetFields, recursive, converter);
    }
    else {
      targetFields.forEach( targetField => {
        if(field === targetField) {
          (input as any)[field] = isNaN(Date.parse(value))? undefined: converter(value);
        }
      });
    }
  })
};

/**
 * Return a clone of the provided object with specified fields converted to type Date. A deep clone
 * will be undertaken, and so nested fields with matching names will also be converted.
 *
 * @param input The object to be processed.
 * @param fields List of the names of fields to convert.
 */
export const convertDateFields = <T>(input: unknown, fields: string[]): T => {
  const inputClone: any = Lodash.cloneDeep(input);
  convertFields(inputClone, fields, true, (value: string) => new Date( value));
  return inputClone;
};

