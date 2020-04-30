import * as React from "react";
import { mount } from "enzyme";
import * as NavigationGuardModule from "../../../tsrc/components/NavigationGuard";
import { Router } from "react-router";
import { createMemoryHistory } from "history";

/**
 * Need to mock the react-router context and function 'push'.
 */
jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    push: jest.fn(),
  }),
}));

const mockBlockNavigationWithBrowser = jest.spyOn(
  NavigationGuardModule,
  "blockNavigationWithBrowser"
);

describe("<NavigationGuard />", () => {
  const renderComponent = (when: boolean) => {
    const history = createMemoryHistory();
    return mount(
      <Router history={history}>
        <NavigationGuardModule.NavigationGuard when={when} />
      </Router>
    );
  };

  it("should call blockNavigationWithBrowser once when the component first mounts", () => {
    renderComponent(true);
    expect(mockBlockNavigationWithBrowser).toHaveBeenCalledTimes(1);
  });

  it("should set 'window.onbeforeunload' to null when the component unmounts", () => {
    const component = renderComponent(true);
    component.unmount();
    expect(window.onbeforeunload).toBeNull();
  });
});
