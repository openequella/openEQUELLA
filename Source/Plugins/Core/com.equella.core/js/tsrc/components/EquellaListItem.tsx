import * as React from "react";
import { ReactNode } from "react";
import {
  ListItem,
  ListItemSecondaryAction,
  ListItemText
} from "@material-ui/core";

interface EquellaListItemProps {
  listItemPrimaryText: string;
  listItemSecondText?: ReactNode;
  listItemAttributes: {};
  secondaryAction?: ReactNode;
  icon?: ReactNode;
}

class EquellaListItem extends React.Component<EquellaListItemProps> {
  render() {
    const {
      listItemAttributes,
      listItemPrimaryText,
      icon,
      listItemSecondText,
      secondaryAction
    } = this.props;

    return (
      <ListItem {...listItemAttributes}>
        {icon}
        <ListItemText
          primary={listItemPrimaryText}
          secondary={listItemSecondText}
        />
        {secondaryAction && (
          <ListItemSecondaryAction>{secondaryAction}</ListItemSecondaryAction>
        )}
      </ListItem>
    );
  }
}

export default EquellaListItem;
