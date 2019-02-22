import CircularProgress from "@material-ui/core/CircularProgress";
import {
  StyleRules,
  Theme,
  WithStyles,
  withStyles
} from "@material-ui/core/styles";
import * as React from "react";

const styles = (theme: Theme) => {
  return {
    container: {
      position: "relative",
      width: "100%",
      height: "100%"
    },
    loader: {
      position: "absolute",
      width: 100,
      height: 100,
      margin: "auto",
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    }
  } as StyleRules;
};

interface LoaderProps {}

type Props = LoaderProps & WithStyles<"container" | "loader">;

class Loader extends React.Component<Props> {
  render() {
    return (
      <div className={this.props.classes.container}>
        <div className={this.props.classes.loader}>
          <CircularProgress size={100} thickness={5} color="secondary" />
        </div>
      </div>
    );
  }
}

export default withStyles(styles)(Loader);
