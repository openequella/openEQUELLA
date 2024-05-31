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
import { ErrorResponse } from "../api/errors";
import { CardContent, Card, Typography } from "@mui/material";
import { languageStrings } from "../util/langstrings";
import { templateDefaults, TemplateUpdateProps } from "./Template";

const StyledDiv = styled("div")(({ theme }) => ({
  display: "flex",
  justifyContent: "center",
  marginTop: theme.spacing(8),
  marginLeft: theme.spacing(2),
  marginRight: theme.spacing(2),
}));

interface ErrorPageProps extends TemplateUpdateProps {
  error: ErrorResponse;
}

export default React.memo(function ErrorPage({
  error: { code, error, error_description },
  updateTemplate,
}: ErrorPageProps) {
  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(languageStrings.errorpage.title)(tp),
    }));
  }, [updateTemplate]);

  return (
    <StyledDiv id="errorPage">
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
    </StyledDiv>
  );
});
