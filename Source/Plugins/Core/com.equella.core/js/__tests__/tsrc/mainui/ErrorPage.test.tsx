import * as React from "react";
import { createMount } from "@material-ui/core/test-utils";
import ErrorPage from "../../../tsrc/mainui/ErrorPage";

jest.mock("@material-ui/core/styles", () => ({
  makeStyles: () => () => ({ errorPage: "mock-classname" }),
}));

describe("<ErrorPage />", () => {
  let mount: ReturnType<typeof createMount>;

  beforeEach(() => {
    mount = createMount();
  });

  afterEach(() => {
    mount.cleanUp();
  });

  it("should render with no code or description", () => {
    const wrapper = mount(
      <ErrorPage error={{ id: "mock-error", error: "example" }} />
    );

    expect(wrapper.find(".mock-classname")).toHaveLength(1);
    expect(wrapper.find("h3")).toHaveLength(1);
    expect(wrapper.find("h3").text()).toContain("example");
  });

  it("should render with code and description", () => {
    const wrapper = mount(
      <ErrorPage
        error={{
          id: "mock-error",
          error: "example",
          code: 404,
          error_description: "mock description",
        }}
      />
    );

    expect(wrapper.find(".mock-classname")).toHaveLength(1);
    expect(wrapper.find("h3")).toHaveLength(1);
    expect(wrapper.find("h3").text()).toContain("example");
    expect(wrapper.find("h3").text()).toContain("404");
    expect(wrapper.find("h5")).toHaveLength(1);
    expect(wrapper.find("h5").text()).toContain("mock description");
  });
});
