/**
 * Return a new array concatenating the provided array and new element
 */
export function addElement<T>(objects: Array<T>, element: T): Array<T> {
  return [...objects, element];
}

/**
 * If an element is found in the provided array, then replace it with a new element,
 * and return a new array; otherwise call 'addElement'.
 */
export function replaceElement<T>(
  objects: Array<T>,
  element: T,
  replaceElement: T
): Array<T> {
  const index = objects.indexOf(element);
  if (index < 0) {
    return addElement(objects, replaceElement);
  }
  const newObjects = [...objects];
  newObjects[index] = replaceElement;
  return newObjects;
}

/**
 * If an element is found in the provided array, then remove this element,
 * and return a new array; otherwise return the provided array.
 */
export function deleteElement<T>(
  objects: Array<T>,
  element: T,
  deleteCount: number
): Array<T> {
  const index = objects.indexOf(element);
  if (index < 0) {
    return objects;
  }
  const newObjects = [...objects];
  newObjects.splice(index, deleteCount);
  return newObjects;
}
