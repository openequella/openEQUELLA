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

import EditIcon from "@mui/icons-material/Edit";
import { Card, CardContent, CardHeader } from "@mui/material";
import { useContext } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";
import DeleteIcon from "@mui/icons-material/Delete";
import CloseIcon from "@mui/icons-material/Close";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { DashboardPageContext } from "../DashboardPageContext";

const {
  edit: editText,
  delete: deleteText,
  close: closeText,
  maximise: maximiseText,
  minimise: minimiseText,
} = languageStrings.common.action;

export interface PortletItemProps extends React.PropsWithChildren {
  /**
   * Basic information about the portlet to be displayed.
   */
  portlet: OEQ.Dashboard.BasicPortlet;
}

/**
 * A wrapper component for a portlet item to be shown on the dashboard page.
 * It displays the portlet name, action icons and the portlet content.
 */
const PortletItem = ({
  portlet: { commonDetails },
  children,
}: PortletItemProps) => {
  const {
    name,
    uuid,
    canEdit,
    canClose,
    canDelete,
    canMinimise,
    isMinimised,
    isInstitutionWide,
  } = commonDetails;

  const { refreshPortlets } = useContext(DashboardPageContext);

  const handleEdit = () => {
    // TODO: redirect to edit page.
  };

  const handleDelete = () => {
    // TODO: add API call
    refreshPortlets();
  };

  const handleClose = () => {
    // TODO: add API call
    refreshPortlets();
  };

  const handleMinimise = (_: boolean) => {
    // TODO: add API call.
    refreshPortlets();
  };

  const minimiseIcon = () =>
    isMinimised ? (
      <TooltipIconButton
        title={maximiseText}
        onClick={() => handleMinimise(false)}
      >
        <ExpandMoreIcon />
      </TooltipIconButton>
    ) : (
      <TooltipIconButton
        title={minimiseText}
        onClick={() => handleMinimise(true)}
      >
        <ExpandLessIcon />
      </TooltipIconButton>
    );

  const actions = () => (
    <>
      {canEdit && (
        <TooltipIconButton title={editText} onClick={handleEdit}>
          <EditIcon />
        </TooltipIconButton>
      )}

      {
        // Only private portlets can be deleted(can't be closed).
        !isInstitutionWide && canDelete && (
          <TooltipIconButton title={deleteText} onClick={handleDelete}>
            <DeleteIcon />
          </TooltipIconButton>
        )
      }

      {
        // Only institution wide portlets can be closed(can't be deleted).
        isInstitutionWide && canClose && (
          <TooltipIconButton title={closeText} onClick={handleClose}>
            <CloseIcon />
          </TooltipIconButton>
        )
      }

      {canMinimise && minimiseIcon()}
    </>
  );

  return (
    <Card>
      <CardHeader title={name} action={actions()} />

      {!isMinimised && (
        <CardContent id={`portlet-content-${uuid}`}>{children}</CardContent>
      )}
    </Card>
  );
};

export default PortletItem;
