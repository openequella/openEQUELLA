import {
  FormControl,
  MenuItem,
  OutlinedInput,
  Select
} from "@material-ui/core";
import { ContentIndex } from "../SearchSettingsModule";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { makeStyles } from "@material-ui/styles";

export interface ContentIndexSettingProps {
  disabled: boolean;
  value: ContentIndex;
  setValue: (indexOption: ContentIndex) => void;
}
const useStyles = makeStyles({
  select: {
    width: "200px"
  }
});
export default function ContentIndexSetting({
  disabled,
  value,
  setValue
}: ContentIndexSettingProps) {
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();
  return (
    <>
      <FormControl variant={"outlined"}>
        <Select
          SelectDisplayProps={{ id: "_contentIndex" }}
          disabled={disabled}
          onChange={event => setValue(event.target.value as ContentIndex)}
          variant={"outlined"}
          value={value}
          className={classes.select}
          input={<OutlinedInput labelWidth={0} id={"_contentIndex"} />}
        >
          <MenuItem value={ContentIndex.OPTION_NONE}>
            {searchPageSettingsStrings.relevance}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_WEBPAGE}>
            {searchPageSettingsStrings.lastModified}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_SECONDARY}>
            {searchPageSettingsStrings.dateCreated}
          </MenuItem>
        </Select>
      </FormControl>
    </>
  );
}
