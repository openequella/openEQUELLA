import * as React from "react";
import {
  List,
  ListItem,
  ListItemText,
  Checkbox,
  Typography,
} from "@material-ui/core";
import { getMimeTypeDetail, MimeTypeEntry } from "./SearchFilterSettingsModule";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../../util/langstrings";

const useStyles = makeStyles({
  item: {
    padding: 0,
  },
  list: {
    maxHeight: 400,
    overflow: "auto",
  },
  listTitle: {
    marginTop: 20,
  },
});

interface MimeTypeFilterListProps {
  /**
   * A list of MIME types to be displayed.
   */
  entries: MimeTypeEntry[];
  /**
   * Fired when the value of each Checkbox is changed.
   */
  onChange(check: boolean, mimeType: string): void;
  /**
   * Values of Checkboxes that are ticked.
   */
  selected: string[];
}

const MimeTypeList = ({
  entries,
  onChange,
  selected,
}: MimeTypeFilterListProps) => {
  const classes = useStyles();
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  return (
    <List
      className={classes.list}
      subheader={
        <Typography
          variant={"subtitle2"}
          color={"textSecondary"}
          className={classes.listTitle}
        >
          {searchFilterStrings.mimetypelistlabel}
        </Typography>
      }
    >
      {entries.map((entry, index) => {
        return (
          <ListItem key={index} className={classes.item}>
            <Checkbox
              checked={selected.indexOf(entry.mimeType) >= 0}
              onChange={(_, checked) => onChange(checked, entry.mimeType)}
            />
            <ListItemText primary={getMimeTypeDetail(entry)} />
          </ListItem>
        );
      })}
    </List>
  );
};

export default React.memo(MimeTypeList);
