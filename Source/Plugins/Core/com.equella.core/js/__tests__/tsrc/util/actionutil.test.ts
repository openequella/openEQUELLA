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
import { actionCreator, wrapAsyncWorker } from "../../../tsrc/util/actionutil";

describe("actionutil", () => {
  describe("actionCreator", () => {
    it("should return a sync action", () => {
      const actionFactory = actionCreator("example");
      expect(typeof actionFactory).toBe("function");
      const action = actionFactory();
      expect(action).toMatchObject({ type: "example", payload: undefined });
    });

    it("should return an async action", () => {
      const actionFactory = actionCreator.async("example");
      expect(typeof actionFactory).toBe("object");

      const action = actionFactory.started("payload");
      expect(action).toMatchObject({
        type: "example_STARTED",
        payload: "payload",
      });
    });
  });

  describe("wrapAsyncWorker", () => {
    it("should do something?", async () => {
      const workerSpy = jest.fn().mockResolvedValue({ example: "value" });
      const asyncActionCreator = actionCreator.async("another-example");
      const wrappedWorker = wrapAsyncWorker(asyncActionCreator, workerSpy);
      expect(typeof wrappedWorker).toBe("function");

      const dispatchSpy = jest.fn();
      const result = await wrappedWorker(dispatchSpy, {});
      expect(workerSpy).toHaveBeenCalled();
      expect(dispatchSpy).toHaveBeenCalled();
      expect(result).toEqual({ example: "value" });
    });
  });
});
