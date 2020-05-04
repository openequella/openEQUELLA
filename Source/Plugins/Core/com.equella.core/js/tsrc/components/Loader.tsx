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
import CircularProgress from "@material-ui/core/CircularProgress";
import {
  StyleRules,
  Theme,
  WithStyles,
  withStyles,
} from "@material-ui/core/styles";
import * as React from "react";

const styles = (theme: Theme) => {
  return {
    container: {
      position: "relative",
      width: "100%",
      height: "100%",
    },
    loader: {
      position: "absolute",
      width: 100,
      height: 100,
      margin: "auto",
      left: 0,
      right: 0,
      top: 0,
      bottom: 0,
    },
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
