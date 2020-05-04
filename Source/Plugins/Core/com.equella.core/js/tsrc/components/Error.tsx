/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
