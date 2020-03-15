/**
 * Check if a number is an integer.
 * Optionally can also validate required/optional values and sign of number
 *
 * @param val number to validate
 * @param required optionally validate that a value is passed
 * @param positive optionally validate the number is positive
 */
export function isInteger(
  val?: number,
  required?: boolean,
  positive?: boolean
): boolean {
  if (typeof val === "undefined") {
    return !required;
  }
  const intVal = parseInt(val.toString(), 10);
  if (positive && intVal <= 0) {
    return false;
  }
  return val === intVal;
}
