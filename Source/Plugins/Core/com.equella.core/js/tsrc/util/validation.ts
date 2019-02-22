export function isInteger(
  val: any,
  required?: boolean,
  positive?: boolean
): boolean {
  const undef = typeof val === "undefined";
  if (required && undef) {
    return false;
  }
  if (!required && undef) {
    return true;
  }
  const intVal = parseInt(val);
  if (positive && intVal <= 0) {
    return false;
  }
  return val === intVal;
}
