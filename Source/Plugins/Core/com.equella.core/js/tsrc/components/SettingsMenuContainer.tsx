import * as React from "react";
import { createStyles, Paper, withStyles, WithStyles } from "@material-ui/core";

const styles = createStyles({
  container: {
    margin: "8px",
    padding: "8px",
  },
});

class SettingsMenuContainer extends React.Component<WithStyles<typeof styles>> {
  constructor(props: WithStyles<typeof styles>) {
    super(props);
  }

  render() {
    const styles = this.props.classes;
    return <Paper className={styles.container}>{this.props.children}</Paper>;
  }
}

export default withStyles(styles)(SettingsMenuContainer);
