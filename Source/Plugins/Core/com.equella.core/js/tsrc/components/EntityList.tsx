import * as React from "react";
import {
  WithStyles,
  Theme,
  Typography,
  Paper,
  List,
  CircularProgress,
  Fab
} from "@material-ui/core";
import { StyleRules, withStyles } from "@material-ui/core/styles";
import { ClickableLink } from "../api";
import AddIcon from "@material-ui/icons/Add";

const styles = (theme: Theme) =>
  ({
    overall: {
      padding: theme.spacing.unit * 2,
      height: "100%"
    },
    results: {
      padding: theme.spacing.unit * 2,
      position: "relative"
    },
    resultHeader: {
      display: "flex",
      justifyContent: "flex-end"
    },
    resultText: {
      flexGrow: 1
    },
    progress: {
      display: "flex",
      justifyContent: "center"
    },
    fab: {
      zIndex: 1000,
      position: "fixed",
      bottom: theme.spacing.unit * 2,
      right: theme.spacing.unit * 5
    }
  } as StyleRules);

interface EntityListProps extends WithStyles<typeof styles> {
  resultsText: React.ReactNode;
  resultsRight?: React.ReactNode;
  children: React.ReactNode;
  createLink?: ClickableLink;
  progress: Boolean;
  id?: string;
}

class EntityList extends React.Component<EntityListProps, {}> {
  render() {
    const {
      id,
      classes,
      progress,
      resultsText,
      resultsRight,
      children,
      createLink
    } = this.props;
    return (
      <div id={id} className={classes.overall}>
        {createLink && (
          <Fab
            className={classes.fab}
            href={createLink.href}
            onClick={createLink.onClick}
            color="secondary"
          >
            <AddIcon />
          </Fab>
        )}
        <Paper className={classes.results}>
          <div className={classes.resultHeader}>
            <Typography className={classes.resultText} variant="subtitle1">
              {resultsText}
            </Typography>
            {resultsRight}
          </div>
          <List>{children}</List>
          {progress && (
            <div className={classes.progress}>
              <CircularProgress />
            </div>
          )}
        </Paper>
      </div>
    );
  }
}

export default withStyles(styles)(EntityList);
