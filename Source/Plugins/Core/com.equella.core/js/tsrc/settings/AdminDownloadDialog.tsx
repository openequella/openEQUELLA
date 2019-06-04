import * as React from "react";
import { Dialog, DialogTitle } from "@material-ui/core";

export default function AdminDownloadDialog(props: {
  open: boolean;
  onClose: () => void;
}) {
  const { open } = props;
  return (
    <Dialog open={open} onClose={props.onClose}>
      <DialogTitle>Please download something</DialogTitle>
    </Dialog>
  );
}
