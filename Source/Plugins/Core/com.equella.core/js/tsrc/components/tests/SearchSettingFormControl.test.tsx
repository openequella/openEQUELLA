import SearchSettingFormControl from "../SearchSettingFormControl";
import { Checkbox, TextField } from "@material-ui/core";
import * as React from "react";
import { shallow } from "enzyme";

test("snapshot test", () => {
  const onChange = jest.fn();
  const textbox = shallow(
    <SearchSettingFormControl
      control={
        <TextField id="testTextField" value={"test"} onChange={onChange} />
      }
      onChange={onChange}
    />
  );
  expect(textbox).toMatchSnapshot();
  const checkbox = shallow(
    <SearchSettingFormControl
      control={<Checkbox id="testCheckbox" value={true} onChange={onChange} />}
      title={"this one has a title"}
      label={"and a label"}
      helperText={"helper text"}
      onChange={onChange}
    />
  );
  expect(checkbox).toMatchSnapshot();
});
