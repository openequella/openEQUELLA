import {
  FormControl,
  MenuItem,
  OutlinedInput,
  Select
} from "@material-ui/core";
import { ContentIndex } from "../SearchSettingsModule";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";

export interface WebPageIndexSettingProps {
  disabled: boolean;
  value: ContentIndex;
  setValue: (indexOption: ContentIndex) => void;
}
export default function WebPageIndexSetting({
  disabled,
  value,
  setValue
}: WebPageIndexSettingProps) {
  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;
  return (
    <>
      <FormControl variant={"outlined"}>
        <Select
          SelectDisplayProps={{ id: "_contentIndex" }}
          disabled={disabled}
          onChange={event => setValue(event.target.value as ContentIndex)}
          variant={"outlined"}
          value={value}
          autoWidth={true}
          input={<OutlinedInput labelWidth={0} id={"_contentIndex"} />}
        >
          <MenuItem value={ContentIndex.OPTION_NONE}>
            {contentIndexSettingsStrings.option.none}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_WEBPAGE}>
            {contentIndexSettingsStrings.option.webPage}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_SECONDARY}>
            {contentIndexSettingsStrings.option.secondaryPage}
          </MenuItem>
        </Select>
      </FormControl>
    </>
  );
}
