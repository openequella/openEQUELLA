import * as React from "react";
import { Button, makeStyles } from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import { Save } from "@material-ui/icons";

const useStyles = makeStyles({
  floatingButton: {
    position: "fixed",
    top: 0,
    right: 0,
    marginTop: "80px",
    marginRight: "16px",
    width: "calc(25% - 112px)",
  },
});

interface SaveButtonProps {
  /**
   * Fired when the Save button is clicked.
   */
  onSave: () => void;
  /**
   * Disable the Save button if true.
   */
  saveButtonDisabled: boolean;
}

const SaveButton = ({ onSave, saveButtonDisabled }: SaveButtonProps) => {
  const classes = useStyles();
  return (
    <Button
      color={"primary"}
      className={classes.floatingButton}
      variant={"contained"}
      size={"large"}
      onClick={onSave}
      aria-label={commonString.action.save}
      disabled={saveButtonDisabled}
    >
      <Save />
      {commonString.action.save}
    </Button>
  );
};

export default SaveButton;
