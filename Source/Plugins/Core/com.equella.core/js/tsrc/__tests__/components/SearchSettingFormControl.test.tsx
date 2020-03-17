import SearchSettingFormControl from "../../components/SearchSettingFormControl";
import { TextField } from "@material-ui/core";
import * as React from "react";
import { mount, ReactWrapper, shallow } from "enzyme";

describe("SearchSettingFormControl.tsx", () => {
  const onChange = jest.fn();
  let component: ReactWrapper;
  beforeEach(() => {
    component = mount(
      shallow(
        <SearchSettingFormControl
          control={
            <TextField id="testTextField" value={"test"} onChange={onChange} />
          }
          onChange={onChange}
        />
      ).get(0)
    );
  });
  afterEach(() => jest.clearAllMocks);

  test("onChange - is not triggered before call", () => {
    expect(onChange.mock.calls.length).toEqual(0);
  });

  test("onChange - is triggered after call", () => {
    const textField = component.find("input");
    textField.simulate("change");
    expect(onChange.mock.calls.length).toEqual(1);
    component.unmount();
  });
});
