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

import { Card, CardContent, CardHeader, Typography } from "@mui/material";
import { styled } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import HTMLReactParser from "html-react-parser";
import * as A from "fp-ts/Array";
import exampleImage from "url:../../assets/dashboard-example.png";

const {
  welcomeTitle,
  welcomeDesc: {
    systemUser: { howToModify: howToModifyText },
    nonSystemUser: {
      hintForOeq: hintForOeqText,
      selectAddButton: selectAddButtonText,
      imageAlt: imageAltText,
    },
  },
} = languageStrings.dashboard;

export interface WelcomeBoardProps {
  /**
   * `true` if the current user is a system user.
   */
  isSystemUser?: boolean;
  /**
   * `true` if the current user has the permission to create portlets.
   */
  hasCreatePortletAcl?: boolean;
}

const DashboardExampleImage = styled("img")(({ theme }) => {
  return {
    [`&`]: {
      maxWidth: "100%",
      padding: theme.spacing(1),
    },
  };
});

/**
 * A welcome board component to be shown on the dashboard page if there is no portlet configured.
 */
const WelcomeBoard = ({
  isSystemUser,
  hasCreatePortletAcl,
}: WelcomeBoardProps) => {
  const welcomeMessages = (paragraphs: string[]) =>
    pipe(
      paragraphs,
      A.mapWithIndex((index, text) => (
        <Typography key={index} sx={{ padding: 1 }}>
          {HTMLReactParser(text)}
        </Typography>
      )),
    );

  const welcomeMessageWithExampleImage = (
    <>
      {welcomeMessages([hintForOeqText, selectAddButtonText])}
      <DashboardExampleImage src={exampleImage} alt={imageAltText} />
    </>
  );

  const welcomeMessagesForNonSystemUser = hasCreatePortletAcl
    ? welcomeMessageWithExampleImage
    : welcomeMessages([hintForOeqText]);

  return (
    <Card>
      <CardHeader
        title={welcomeTitle}
        // Same size as the old UI.
        slotProps={{
          title: {
            variant: "h4",
          },
        }}
      />

      <CardContent
        // Same padding as the old UI.
        sx={{ paddingX: 5, paddingTop: 0 }}
      >
        {isSystemUser
          ? welcomeMessages([howToModifyText])
          : welcomeMessagesForNonSystemUser}
      </CardContent>
    </Card>
  );
};

export default WelcomeBoard;
