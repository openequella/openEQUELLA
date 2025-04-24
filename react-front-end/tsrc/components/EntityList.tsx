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
import { styled } from "@mui/material/styles";
import { CircularProgress, Fab, List, Paper, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

const PREFIX = "EntityList";

const classes = {
  overall: `${PREFIX}-overall`,
  results: `${PREFIX}-results`,
  resultHeader: `${PREFIX}-resultHeader`,
  resultText: `${PREFIX}-resultText`,
  progress: `${PREFIX}-progress`,
  fab: `${PREFIX}-fab`,
};

const Root = styled("div")(({ theme }) => ({
  [`& .${classes.overall}`]: {
    padding: theme.spacing(2),
    height: "100%",
  },

  [`& .${classes.results}`]: {
    padding: theme.spacing(2),
    position: "relative",
  },

  [`& .${classes.resultHeader}`]: {
    display: "flex",
    justifyContent: "flex-end",
  },

  [`& .${classes.resultText}`]: {
    flexGrow: 1,
  },

  [`& .${classes.progress}`]: {
    display: "flex",
    justifyContent: "center",
  },

  [`& .${classes.fab}`]: {
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
  progress: boolean;
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
  return (
    <Root id={id} className={classes.overall}>
      {createOnClick && (
        <Fab
          id="add-entity"
          className={classes.fab}
          component="button"
          color="secondary"
          onClick={createOnClick}
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
    </Root>
  );
};

export default EntityList;
