/**
 * Validate and transform a string to a Date
 */
export const toValidDate = (value: string): Date => {
  if (isNaN(Date.parse(value))) {
    throw TypeError(`Invalid Date: "${value}"`);
  }
  return new Date(value);
};
