import * as React from "react";
import { FunctionComponent, ReactNode } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";

interface ConfirmDialogProps {
  /**
   * Open the dialog when true.
   */
  open: boolean;
  /**
   * The title of the dialog.
   */
  title: string;
  /**
   * Contents displayed in the dialog.
   */
  children?: ReactNode;
  /**
   * Fired when click the Confirm button.
   */
  onConfirm: () => void;
  /**
   * Fired when click the Cancel button.
   */
  onCancel: () => void;
  /**
   * The text of the Confirm button.
   */
  confirmButtonText: string;
}

const ConfirmDialog: FunctionComponent<ConfirmDialogProps> = ({
  open,
  title,
  children,
  onCancel,
  onConfirm,
  confirmButtonText,
}: ConfirmDialogProps) => {
  const { cancel } = commonString.action;
  return (
    <Dialog
      open={open}
      onClose={onCancel}
      aria-labelledby="alert-dialog-title"
      aria-describedby="alert-dialog-description"
    >
      <DialogTitle id="alert-dialog-title">{title}</DialogTitle>
      <DialogContent>
        <DialogContentText id="alert-dialog-description">
          {children}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={onCancel}
          color="secondary"
          id="confirm-dialog-cancel-button"
        >
          {cancel}
        </Button>
        <Button
          onClick={onConfirm}
          color="primary"
          id="confirm-dialog-confirm-button"
          autoFocus
        >
          {confirmButtonText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
