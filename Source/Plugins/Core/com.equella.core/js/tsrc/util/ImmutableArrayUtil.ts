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
/**
 * Return a new array concatenating the provided array and new element.
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
  comparator: (element: T) => void,
  replaceElement: T
): Array<T> {
  const index = objects.findIndex(comparator);
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
  comparator: (element: T) => void,
  deleteCount: number
): Array<T> {
  const index = objects.findIndex(comparator);
  if (index < 0) {
    return objects;
  }
  const newObjects = [...objects];
  newObjects.splice(index, deleteCount);
  return newObjects;
}
