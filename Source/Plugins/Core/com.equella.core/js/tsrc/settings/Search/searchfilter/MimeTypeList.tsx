import * as React from "react";
import List from "@material-ui/core/List";
import { ListItem, ListItemText } from "@material-ui/core";
import Checkbox from "@material-ui/core/Checkbox";
import { getMimeTypeDetail, MimeTypeEntry } from "./SearchFilterSettingsModule";
import { makeStyles } from "@material-ui/styles";

const useStyles = makeStyles({
  item: {
    padding: 0
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
    return (
      <List>
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
    );
  }
);
