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
import * as React from "react";
import CircularProgress from "@material-ui/core/CircularProgress";
import { makeStyles } from "@material-ui/core/styles";

const useStyles = makeStyles(() => ({
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
}));

const Loader = () => {
  const styles = useStyles();
  return (
    <div className={styles.container}>
      <div className={styles.loader}>
        <CircularProgress size={100} thickness={5} color="secondary" />
      </div>
    </div>
  );
};

export default Loader;
