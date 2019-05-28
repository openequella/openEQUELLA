import * as React from "react";
import { languageStrings } from "../util/langstrings";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  Button,
  DialogActions
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";

export const strings = languageStrings.template;

export function defaultNavMessage() {
  return strings.navaway.content;
}

export const NavAwayDialog = React.memo(function NavAwayDialog(props: {
  message: string;
  open: boolean;
  navigateConfirm: (confirmed: boolean) => void;
}) {
  const { navigateConfirm, open, message } = props;
  return (
    <Dialog open={open}>
      <DialogTitle>{strings.navaway.title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button color="secondary" onClick={_ => navigateConfirm(false)}>
          {commonString.action.cancel}
        </Button>
        <Button color="primary" onClick={_ => navigateConfirm(true)}>
          {commonString.action.discard}
        </Button>
      </DialogActions>
    </Dialog>
  );
});
