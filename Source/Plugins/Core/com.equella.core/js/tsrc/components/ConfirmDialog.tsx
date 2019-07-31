import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button
} from "@material-ui/core";
import * as React from "react";
import { commonString } from "../util/commonstrings";

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  onConfirm: () => void;
  onCancel: () => void;
}

class ConfirmDialog extends React.Component<ConfirmDialogProps> {
  constructor(props: ConfirmDialogProps) {
    super(props);
  }

  render() {
    const { open, title, children, onCancel, onConfirm } = this.props;
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
  }
}

export default ConfirmDialog;
