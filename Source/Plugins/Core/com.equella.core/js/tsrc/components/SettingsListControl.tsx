import {
  ListItem,
  ListItemSecondaryAction,
  ListItemText
} from "@material-ui/core";
import * as React from "react";
import { ReactNode } from "react";
import { makeStyles } from "@material-ui/styles";

interface SettingsListControlProps {
  divider?: boolean;
  primaryText?: string;
  secondaryText?: string;
  control?: ReactNode;
}

const useStyles = makeStyles({
  listItemText: {
    maxWidth: "65%"
  }
});
/*
 * This component is used to define a row inside a SettingsList to be used in the page/settings/* pages.
 * It should be placed within a SettingsList.
 *
 * divider : if present, the row will be rendered with a divider at the bottom.
 * primaryText: Text to appear on the top line of the row.
 * secondaryText: Text to appear on the bottom line(s) of the row.
 * control: The controllable component to be rendered on the right hand side of the row.
 */
export default function SettingsListControl({
  divider,
  primaryText,
  secondaryText,
  control
}: SettingsListControlProps) {
  const classes = useStyles();
  return (
    <ListItem alignItems={"flex-start"} divider={divider}>
      <ListItemText
        className={classes.listItemText}
        primary={primaryText}
        secondary={secondaryText}
      />
      <ListItemSecondaryAction>{control}</ListItemSecondaryAction>
    </ListItem>
  );
}
