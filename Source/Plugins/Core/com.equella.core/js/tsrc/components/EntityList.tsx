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
import {
  CircularProgress,
  Fab,
  List,
  Paper,
  Theme,
  Typography,
  makeStyles,
} from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";

const useStyles = makeStyles((theme: Theme) => ({
  overall: {
    padding: theme.spacing(2),
    height: "100%",
  },
  results: {
    padding: theme.spacing(2),
    position: "relative",
  },
  resultHeader: {
    display: "flex",
    justifyContent: "flex-end",
  },
  resultText: {
    flexGrow: 1,
  },
  progress: {
    display: "flex",
    justifyContent: "center",
  },
  fab: {
    zIndex: 1000,
    position: "fixed",
    bottom: theme.spacing(2),
    right: theme.spacing(5),
  },
}));

interface EntityListProps {
  resultsText: React.ReactNode;
  resultsRight?: React.ReactNode;
  children: React.ReactNode;
  createOnClick?: () => void;
  progress: Boolean;
  id?: string;
}

const EntityList = ({
  id,
  progress,
  resultsText,
  resultsRight,
  children,
  createOnClick,
}: EntityListProps) => {
  const styles = useStyles();
  return (
    <div id={id} className={styles.overall}>
      {createOnClick && (
        <Fab
          id="add-entity"
          className={styles.fab}
          component="button"
          color="secondary"
          onClick={createOnClick}
        >
          <AddIcon />
        </Fab>
      )}
      <Paper className={styles.results}>
        <div className={styles.resultHeader}>
          <Typography className={styles.resultText} variant="subtitle1">
            {resultsText}
          </Typography>
          {resultsRight}
        </div>
        <List>{children}</List>
        {progress && (
          <div className={styles.progress}>
            <CircularProgress />
          </div>
        )}
      </Paper>
    </div>
  );
};

export default EntityList;
