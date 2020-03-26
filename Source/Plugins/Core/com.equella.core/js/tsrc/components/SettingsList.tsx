import { List, ListSubheader } from "@material-ui/core";
import * as React from "react";
import { ReactNode } from "react";

interface SettingsListProps {
  subHeading?: string;
  children: ReactNode;
}
/*
 * This component is used to define a settings list to be used in the page/settings/* pages.
 * It should be placed within a Card,
 * and the children of this components should be SettingsListControls.
 * subHeading: optional title of the settings list.
 * children: 0 or more SettingsListControls representing the rows of this list.
 */
export default function SettingsList({
  subHeading,
  children
}: SettingsListProps) {
  return (
    <List
      subheader={<ListSubheader disableGutters>{subHeading}</ListSubheader>}
    >
      {children}
    </List>
  );
}
