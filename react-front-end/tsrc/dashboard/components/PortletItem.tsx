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
import { pipe } from "fp-ts/function";
import { useContext } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { AppContext } from "../../mainui/App";
import { editPortlet } from "../../modules/DashboardModule";
import { languageStrings } from "../../util/langstrings";
import DeleteIcon from "@mui/icons-material/Delete";
import CloseIcon from "@mui/icons-material/Close";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { DashboardPageContext } from "../DashboardPageContext";
import PortletItemSkeleton from "./PortletItemSkeleton";
import { useHistory } from "react-router";
import * as TE from "fp-ts/TaskEither";

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
  /**
   * `true` if the portlet is in the process of being loaded,
   * and show a loading skeleton instead of the actual content.
   */
  isLoading?: boolean;
  /*
   * An optional render function that returns the portlet content when `isLoading` is false.
   *
   * This function provides a more expressive and readable alternative
   * to the `children` prop. Use it when the portlet's content involves
   * lazily/conditional rendering logic. Functionally equivalent
   * to providing `children` directly.
   */
  renderChildren?: () => React.ReactNode;
}

/**
 * A wrapper component for a portlet item to be shown on the dashboard page.
 * It displays the portlet name, action icons and the portlet content.
 */
const PortletItem = ({
  portlet: { commonDetails },
  children,
  isLoading,
  renderChildren,
}: PortletItemProps) => {
  const {
    name,
    uuid,
    canEdit,
    canClose,
    canDelete,
    canMinimise,
    isInstitutionWide,
    isMinimised,
    isClosed,
    order,
    column,
  } = commonDetails;

  const { closePortlet, deletePortlet, minimisePortlet } =
    useContext(DashboardPageContext);
  const history = useHistory();
  const { appErrorHandler } = useContext(AppContext);

  const handleEdit = () => {
    pipe(
      TE.tryCatch(
        () => editPortlet(uuid),
        (e) => `Failed to edit ${name} portlet: ${e}`,
      ),
      TE.match(appErrorHandler, (path) => {
        const sep = path.includes("?") ? "&" : "?";
        history.push(`${path}${sep}portletEditor=true`);
      }),
    )();
  };

  const handleDelete = () => deletePortlet(uuid);

  const handleClose = () => closePortlet(uuid);

  const handleMinimise = (newIsMinimised: boolean) =>
    minimisePortlet(uuid, {
      order,
      column,
      isClosed,
      isMinimised: newIsMinimised,
    });

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

  return isLoading ? (
    <PortletItemSkeleton />
  ) : (
    <Card>
      <CardHeader title={name} action={actions()} />

      {!isMinimised && (
        <CardContent id={`portlet-content-${uuid}`}>
          {renderChildren ? renderChildren() : children}
        </CardContent>
      )}
    </Card>
  );
};

export default PortletItem;
