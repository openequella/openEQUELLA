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
