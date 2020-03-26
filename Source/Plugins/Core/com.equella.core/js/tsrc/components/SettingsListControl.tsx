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
