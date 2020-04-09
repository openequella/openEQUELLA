import * as React from "react";
import {
  List,
  ListItem,
  ListItemText,
  Checkbox,
  Typography
} from "@material-ui/core";
import { getMimeTypeDetail, MimeTypeEntry } from "./SearchFilterSettingsModule";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../../util/langstrings";

const useStyles = makeStyles({
  item: {
    padding: 0
  },
  list: {
    maxHeight: 400,
    overflow: "auto"
  },
  listTitle: {
    marginTop: 20
  }
});

interface MimeTypeFilterListProps {
  entries: MimeTypeEntry[];
  onChange(check: boolean, mimeType: string): void;
  selected?: string[];
}

export const MimeTypeList = React.memo(
  ({ entries, onChange, selected }: MimeTypeFilterListProps) => {
    const classes = useStyles();
    const searchFilterStrings =
      languageStrings.settings.searching.searchfiltersettings;

    return (
      <div className={classes.list}>
        <List
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
          {entries.map(entry => {
            let checked = false;
            if (selected && selected.indexOf(entry.mimeType) >= 0) {
              checked = true;
            }
            return (
              <ListItem key={entry.mimeType} className={classes.item}>
                <Checkbox
                  checked={checked}
                  onChange={(_, checked) => onChange(checked, entry.mimeType)}
                />
                <ListItemText primary={getMimeTypeDetail(entry)} />
              </ListItem>
            );
          })}
        </List>
      </div>
    );
  }
);
