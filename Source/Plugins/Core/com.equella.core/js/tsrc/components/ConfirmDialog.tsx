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
  open: boolean;
  title: string;
  children?: ReactNode;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmDialog: FunctionComponent<ConfirmDialogProps> = ({
  open,
  title,
  children,
  onCancel,
  onConfirm,
}: ConfirmDialogProps) => {
  const { cancel, delete: del } = commonString.action;
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
        <Button onClick={onCancel} color="secondary" id="cancel-delete">
          {cancel}
        </Button>
        <Button
          onClick={onConfirm}
          color="primary"
          id="confirm-delete"
          autoFocus
        >
          {del}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
