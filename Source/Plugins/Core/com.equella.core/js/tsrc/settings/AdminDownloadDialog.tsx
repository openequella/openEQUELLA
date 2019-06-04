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

export default function AdminDownloadDialog(props: {
  open: boolean;
  onClose: () => void;
}) {
  const { open, onClose } = props;
  const { ok } = languageStrings.common.action;
  const { link, text, title } = languageStrings.adminconsoledownload;

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>
          {text.partOne}
          <a href={link} target="_blank">
            {text.partTwo}
          </a>
          {text.partThree}
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
