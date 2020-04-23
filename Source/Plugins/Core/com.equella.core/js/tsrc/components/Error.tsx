import { Typography } from "@material-ui/core";
import Paper from "@material-ui/core/Paper";
import { Theme, WithStyles, withStyles } from "@material-ui/core/styles";
import * as React from "react";

const styles = (theme: Theme) => {
  return {
    error: {
      padding: theme.spacing(3),
      backgroundColor: "rgb(255, 220, 220)",
    },
  };
};

interface ErrorProps {}

type Props = ErrorProps & WithStyles<"error">;

class Error extends React.Component<Props> {
  render() {
    return (
      <Paper className={this.props.classes.error}>
        <Typography color="error" align="center">
          {this.props.children}
        </Typography>
      </Paper>
    );
  }
}

export default withStyles(styles)(Error);
