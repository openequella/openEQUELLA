export interface IDictionary<T> {
  [key: string]: T | undefined;
}

/**
 * Get a list of key/property names from an object
 *
 * @param obj object to find keys for
 *
 * TODO: replace this with Object.keys or Object.getOwnPropertyNames
 */
export function properties<T>(obj: IDictionary<T>): string[] {
  const props: string[] = [];
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      props.push(key);
    }
  }
  return props;
}
