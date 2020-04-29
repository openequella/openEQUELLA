import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Typography,
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";

interface MessageDialogProps {
  open: boolean;
  title: string;
  subtitle: string;
  messages: string[];
  close: () => void;
}
const MessageDialog = ({
  open,
  title,
  subtitle,
  messages,
  close,
}: MessageDialogProps) => {
  return (
    <Dialog open={open} onClose={close} fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{subtitle}</DialogContentText>
        {messages.map((message) => (
          <Typography>{message}</Typography>
        ))}
      </DialogContent>
      <DialogActions>
        <Button onClick={close} color="primary">
          {commonString.action.ok}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default MessageDialog;
