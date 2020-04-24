import {
  FormControl,
  MenuItem,
  OutlinedInput,
  Select,
} from "@material-ui/core";
import { SortOrder } from "../SearchSettingsModule";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { makeStyles } from "@material-ui/styles";

export interface DefaultSortOrderSettingProps {
  disabled: boolean;
  value: SortOrder;
  setValue: (order: SortOrder) => void;
}
const useStyles = makeStyles({
  select: {
    width: "200px",
  },
});
export default function DefaultSortOrderSetting({
  disabled,
  value,
  setValue,
}: DefaultSortOrderSettingProps) {
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();
  return (
    <>
      <FormControl variant={"outlined"}>
        <Select
          SelectDisplayProps={{ id: "_sortOrder" }}
          disabled={disabled}
          onChange={(event) => setValue(event.target.value as SortOrder)}
          variant={"outlined"}
          value={value}
          className={classes.select}
          input={<OutlinedInput labelWidth={0} id={"_sortOrder"} />}
        >
          <MenuItem value={SortOrder.RANK}>
            {searchPageSettingsStrings.relevance}
          </MenuItem>
          <MenuItem value={SortOrder.DATEMODIFIED}>
            {searchPageSettingsStrings.lastModified}
          </MenuItem>
          <MenuItem value={SortOrder.DATECREATED}>
            {searchPageSettingsStrings.dateCreated}
          </MenuItem>
          <MenuItem value={SortOrder.NAME}>
            {searchPageSettingsStrings.title}
          </MenuItem>
          <MenuItem value={SortOrder.RATING}>
            {searchPageSettingsStrings.userRating}
          </MenuItem>
        </Select>
      </FormControl>
    </>
  );
}
