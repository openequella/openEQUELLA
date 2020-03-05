import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from "@material-ui/core";
import { languageStrings } from "../util/langstrings";

interface AdminDownloadDialogProps {
  open: boolean;
  onClose: () => void;
}

export default function AdminDownloadDialog({
  open,
  onClose
}: AdminDownloadDialogProps) {
  const { ok } = languageStrings.common.action;
  const { link, text, title } = languageStrings.adminconsoledownload;

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>
          {text.introTextOne}
          <a href={link} target="_blank">
            {text.introTextTwo}
          </a>
          {text.introTextThree}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          {ok}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
