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
import CloseIcon from "@mui/icons-material/Close";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {
  alpha,
  Card,
  CardContent,
  CardHeader,
  CardProps,
  Typography,
} from "@mui/material";
import { styled, SxProps, Theme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useRef } from "react";
import { useHistory } from "react-router";
import { sprintf } from "sprintf-js";
import ConfirmDialog from "../../components/ConfirmDialog";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { AppContext } from "../../mainui/App";
import { editPortlet } from "../../modules/DashboardModule";
import { languageStrings } from "../../util/langstrings";
import { DashboardPageContext } from "../DashboardPageContext";
import PortletItemSkeleton from "./PortletItemSkeleton";

const { useState } = React;

const {
  edit: editText,
  delete: deleteText,
  close: closeText,
  maximise: maximiseText,
  minimise: minimiseText,
} = languageStrings.common.action;
const {
  close: closeAlertTitle,
  delete: deleteAlertTitle,
  closeAlert,
  deleteAlert,
  closeAlertInfo,
} = languageStrings.dashboard.portlets.dialog;

interface StyledCardProps extends CardProps {
  /**
   * Whether to apply a highlight effect to the card, used for newly restored portlet.
   */
  highlight: boolean;
}

const StyledCard = styled(Card, {
  shouldForwardProp: (prop) => prop !== "highlight",
})<StyledCardProps>(({ theme, highlight }) => {
  return {
    transition: "background-color 3000ms ease-in-out",
    ...(highlight && {
      backgroundColor: alpha(theme.palette.secondary.main, 0.25),
    }),
  };
});

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
  /**
   * Custom styles to be applied to the `Card` where portlet content and portlet header
   * section are rendered.
   */
  sx?: SxProps<Theme>;
  /**
   * Whether to apply a highlight effect to the card, used for newly restored portlet.
   */
  highlight: boolean;
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
  sx,
  highlight,
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

  type ActionType = "Delete" | "Close";

  const { appErrorHandler } = useContext(AppContext);
  const {
    closePortlet,
    deletePortlet,
    minimisePortlet,
    scrollToRestoredPortletAndReset,
  } = useContext(DashboardPageContext);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [actionType, setActionType] = useState<ActionType>("Close");
  const history = useHistory();
  const cardRef = useRef(null);

  // When the portlet is highlighted (i.e., just restored), scroll it into view
  // After scrolling, reset the restored portlet state.
  useEffect(() => {
    if (highlight && cardRef.current) {
      scrollToRestoredPortletAndReset(cardRef.current);
    }
  }, [highlight, scrollToRestoredPortletAndReset]);

  const handleEdit = () => {
    pipe(
      TE.tryCatch(() => editPortlet(uuid), String),
      TE.match(appErrorHandler, (path) => history.push(path)),
    )();
  };

  const handleDelete = () => {
    deletePortlet(uuid);
    closeDialog();
  };

  const handleClose = () => {
    closePortlet(uuid, { order, column, isMinimised, isClosed: true });
    closeDialog();
  };

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
          <TooltipIconButton
            title={deleteText}
            onClick={() => openDialog("Delete")}
          >
            <DeleteIcon />
          </TooltipIconButton>
        )
      }

      {
        // Only institution wide portlets can be closed(can't be deleted).
        isInstitutionWide && canClose && (
          <TooltipIconButton
            title={closeText}
            onClick={() => openDialog("Close")}
          >
            <CloseIcon />
          </TooltipIconButton>
        )
      }

      {canMinimise && minimiseIcon()}
    </>
  );

  const isCloseAction = actionType === "Close";

  const openDialog = (actionType: ActionType) => {
    setDialogOpen(true);
    setActionType(actionType);
  };

  const closeDialog = () => setDialogOpen(false);

  const renderPortletContent = () => {
    if (isMinimised) return null;
    if (isLoading) return <PortletItemSkeleton />;
    return (
      <CardContent id={`portlet-content-${uuid}`}>
        {renderChildren ? renderChildren() : children}
      </CardContent>
    );
  };

  return (
    <>
      <StyledCard sx={sx} highlight={highlight} ref={cardRef}>
        <CardHeader title={name} action={actions()} />
        {renderPortletContent()}
      </StyledCard>

      <ConfirmDialog
        open={dialogOpen}
        title={isCloseAction ? closeAlertTitle : deleteAlertTitle}
        onConfirm={isCloseAction ? handleClose : handleDelete}
        onCancel={closeDialog}
        confirmButtonText={languageStrings.common.action.ok}
      >
        {isCloseAction ? (
          <>
            <Typography>{sprintf(closeAlert, name)}</Typography>
            <Typography variant="body2" sx={{ mt: 2 }}>
              {closeAlertInfo}
            </Typography>
          </>
        ) : (
          sprintf(deleteAlert, name)
        )}
      </ConfirmDialog>
    </>
  );
};

export default PortletItem;
