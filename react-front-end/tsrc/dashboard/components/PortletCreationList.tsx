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
import { List, ListItem, ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import AddIcon from "@mui/icons-material/Add";
import { pipe } from "fp-ts/function";
import { useCallback, useContext } from "react";
import * as React from "react";
import { useHistory } from "react-router";
import { sprintf } from "sprintf-js";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { AppContext } from "../../mainui/App";
import { getLegacyPortletCreationPageRoute } from "../../modules/DashboardModule";
import * as TE from "fp-ts/TaskEither";
import { languageStrings } from "../../util/langstrings";

const {
  errors: { failedToOpenCreationPage },
} = languageStrings.dashboard;

interface PortletCreationListProps {
  /**
   * A list of portlet types which the current user can create.
   */
  creatablePortletTypes: OEQ.Dashboard.PortletCreatable[];
}

/**
 * Renders a list of portlet types where each list item shows portlet name, description and
 * an Add icon button to open the legacy portlet creation page.
 */
export const PortletCreationList = ({
  creatablePortletTypes,
}: PortletCreationListProps) => {
  const history = useHistory();
  const { appErrorHandler } = useContext(AppContext);

  const createPortlet = useCallback(
    (portletType: OEQ.Dashboard.PortletType) =>
      pipe(
        TE.tryCatch(
          () => getLegacyPortletCreationPageRoute(portletType),
          (e) => sprintf(failedToOpenCreationPage, `${e}`),
        ),
        TE.match(appErrorHandler, (route) => history.push(route)),
      )(),
    [appErrorHandler, history],
  );

  return (
    <List>
      {creatablePortletTypes.map(({ name, desc, portletType }) => (
        <ListItem
          key={portletType}
          secondaryAction={
            <TooltipIconButton
              title={name}
              onClick={() => createPortlet(portletType)}
            >
              <AddIcon />
            </TooltipIconButton>
          }
        >
          <ListItemText primary={name} secondary={desc} />
        </ListItem>
      ))}
    </List>
  );
};
