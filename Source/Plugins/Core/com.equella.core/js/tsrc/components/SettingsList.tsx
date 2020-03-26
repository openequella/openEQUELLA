import { List, ListSubheader } from "@material-ui/core";
import * as React from "react";
import { ReactNode } from "react";

interface SettingsListProps {
  subHeading?: string;
  children: ReactNode;
}

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
