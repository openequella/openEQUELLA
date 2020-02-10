import {
  IconButton,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  Theme,
  WithStyles,
  withStyles,
  createStyles
} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import DeleteIcon from "@material-ui/icons/Delete";
import * as React from "react";
import { LocationDescriptor } from "history";
import { Link } from "react-router-dom";

const styles = (theme: Theme) =>
  createStyles({
    searchResultContent: {
      marginTop: theme.spacing.unit
    },
    itemThumb: {
      maxWidth: "88px",
      maxHeight: "66px",
      marginRight: "12px"
    },
    displayNode: {
      padding: 0
    },
    details: {
      marginTop: theme.spacing.unit
    }
  });

export interface SearchResultExtraDetail {
  label: string;
  value: string;
}

export interface SearchResultProps {
  onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
  to: LocationDescriptor;
  onDelete?: () => void;
  primaryText: string;
  secondaryText?: string;
}

type PropsWithStyles = SearchResultProps &
  WithStyles<"searchResultContent" | "itemThumb" | "displayNode" | "details">;

class SearchResult extends React.Component<PropsWithStyles> {
  render() {
    const { onDelete, to } = this.props;
    const link = (
      <Typography
        color="primary"
        variant="subtitle1"
        component={p => (
          <Link {...p} to={to!}>
            {this.props.primaryText}
          </Link>
        )}
      />
    );

    const content = (
      <Typography
        variant="body1"
        className={this.props.classes.searchResultContent}
      >
        {this.props.secondaryText}
      </Typography>
    );

    return (
      <ListItem button onClick={this.props.onClick} divider>
        <ListItemText disableTypography primary={link} secondary={content} />
        <ListItemSecondaryAction>
          {onDelete && (
            <IconButton onClick={onDelete}>
              <DeleteIcon />
            </IconButton>
          )}
        </ListItemSecondaryAction>
      </ListItem>
    );
  }
}

export default withStyles(styles)(SearchResult);
