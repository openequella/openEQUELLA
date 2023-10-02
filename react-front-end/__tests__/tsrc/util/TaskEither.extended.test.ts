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
import { pipe } from "fp-ts/function";
import * as TE from "../../../tsrc/util/TaskEither.extended";

describe("TaskEither Extended", () => {
  describe("getOrThrow", () => {
    it("should return the right value if TaskEither is right", async () => {
      const rightValue = "Right Value";
      const task = TE.right(rightValue);

      const result = await pipe(task, TE.getOrThrow)();

      expect(result).toEqual(rightValue);
    });

    it("should throw an error if TaskEither is left", async () => {
      const leftValue = "Error Message";
      const task = TE.left(leftValue);

      const result = pipe(task, TE.getOrThrow)();

      await expect(result).rejects.toThrow(new Error(leftValue));
    });
  });
});
