import { List, ListSubheader } from "@material-ui/core";
import * as React from "react";
import { ReactNode } from "react";

interface SettingsListProps {
  /**
   * Optional subheading. Appears above the list at the top left.
   */
  subHeading?: string;
  /**
   * The children of this component - should be zero or more SettingsListControls.
   */
  children: ReactNode;
}
/**
 * This component is used to define a settings list to be used in the page/settings/* pages.
 *
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
