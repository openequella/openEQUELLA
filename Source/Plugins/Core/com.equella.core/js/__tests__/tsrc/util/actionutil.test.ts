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
