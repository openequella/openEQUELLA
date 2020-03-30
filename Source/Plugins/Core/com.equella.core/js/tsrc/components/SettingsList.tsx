import { List, ListSubheader } from "@material-ui/core";
import * as React from "react";

interface SettingsListProps {
  subHeading?: string;
  children: React.ReactNode;
}
/*
 * This component is used to define a settings list to be used in the page/settings/* pages.
 * The children of this components should be SettingsListControls.
 * @param subHeading  optional title of the settings list.
 * @param children    0 or more SettingsListControls representing the rows of this list.
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
