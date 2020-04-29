import * as React from "react";
import { mount } from "enzyme";
import * as module from "../../../tsrc/components/NavigationGuard";
import { Router } from "react-router";
import * as historyModule from "history";
import { act } from "react-dom/test-utils";
import ConfirmDialog from "../../../tsrc/components/ConfirmDialog";

/**
 * Need to mock the react-router context and function 'push'.
 */
jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    push: jest.fn(),
  }),
}));

describe("<NavigationGuard />", () => {
  const history = historyModule.createMemoryHistory();
  const mockBlockNavigationWithBrowser = jest.spyOn(
    module,
    "blockNavigationWithBrowser"
  );
  const component = mount(
    <Router history={history}>
      <module.NavigationGuard when />
    </Router>
  );

  it("should call blockNavigationWithBrowser once in the useEffect", () => {
    expect(component.find(ConfirmDialog)).toHaveLength(1);
    expect(mockBlockNavigationWithBrowser).toHaveBeenCalledTimes(1);
  });

  it("should show ConfirmDialog when route is going to change", () => {
    // The dialog is closed.
    expect(component.exists("h2")).not.toBe(true);

    // The dialog is open when route change event is triggered.
    history.listen(() => {
      expect(component.find("h2").text()).toBe("Close without saving?");
    });
    act(() => history.push("home.do"));
  });
});
