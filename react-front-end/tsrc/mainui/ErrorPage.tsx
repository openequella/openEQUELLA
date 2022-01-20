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
import { ErrorResponse } from "../api/errors";
import { CardContent, Card, makeStyles, Typography } from "@material-ui/core";

const useStyles = makeStyles((t) => ({
  errorPage: {
    display: "flex",
    justifyContent: "center",
    marginTop: t.spacing(8),
    marginLeft: t.spacing(2),
    marginRight: t.spacing(2),
  },
}));

interface ErrorPageProps {
  error: ErrorResponse;
}

export default React.memo(function ErrorPage({
  error: { code, error, error_description },
}: ErrorPageProps) {
  const classes = useStyles();
  return (
    <div id="errorPage" className={classes.errorPage}>
      <Card>
        <CardContent>
          <Typography variant="h3" color="error">
            {code && `${code} : `}
            {error}
          </Typography>
          {error_description && (
            <Typography variant="h5">{error_description}</Typography>
          )}
        </CardContent>
      </Card>
    </div>
  );
});
