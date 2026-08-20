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
import { flow } from "fp-ts/function";

// These wrappers exist to keep `encode` and `decode` called as methods. Handing
// `new TextEncoder().encode` straight to `flow` passes the method on its own, without the instance
// it needs as `this`, and the call then fails with:
//   TypeError: Value of "this" must be of type TextEncoder
const encodeUtf8 = (value: string): Uint8Array =>
  new TextEncoder().encode(value);

const decodeUtf8 = (bytes: Uint8Array): string =>
  new TextDecoder().decode(bytes);

/**
 * Encodes a string as standard base64.
 *
 * `btoa` only accepts code points in the Latin-1 range, so the string is first encoded as UTF-8
 * bytes - each of which is a valid Latin-1 code point. Handing a string containing any character
 * outside that range straight to `btoa` raises an `InvalidCharacterError` instead.
 *
 * Note the result uses the standard base64 alphabet, so it may contain `+`, `/` and `=`. Encode it
 * further where the context it is used in requires a narrower set.
 */
export const encodeBase64: (value: string) => string = flow(
  encodeUtf8,
  (bytes) => String.fromCharCode(...bytes),
  btoa,
);

/**
 * Decodes a standard base64 string, reversing {@link encodeBase64}.
 *
 * @throws InvalidCharacterError where the input is not valid base64.
 */
export const decodeBase64: (value: string) => string = flow(
  atob,
  (binary) => Uint8Array.from(binary, (char) => char.charCodeAt(0)),
  decodeUtf8,
);
