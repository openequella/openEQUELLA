import * as React from "react";
import { ReactNode } from "react";
import { commonString } from "../util/commonstrings";
import MessageInfo from "./MessageInfo";
import { NavigationGuard } from "./NavigationGuard";
import { Button, makeStyles } from "@material-ui/core";
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

export interface SettingPageTemplateProps {
  /**
   * Fired when the Save button is clicked.
   */
  onSave: () => void;
  /**
   * Disable the Save button if true.
   */
  saveButtonDisabled: boolean;
  /**
   * Open the snack bar if true.
   */
  snackbarOpen: boolean;
  /**
   * Fired when the snack bar is closed.
   */
  snackBarOnClose: () => void;
  /**
   * Prevent navigate to different pages if true.
   */
  preventNavigation: boolean;
  /**
   * Child components wrapped in this template.
   */
  children: ReactNode;
}

/**
 * This component is a top level template for Setting pages.
 * It renders child components as well as a Save button, a snack bar
 * and a dialog preventing navigation.
 */
const SettingPageTemplate = ({
  onSave,
  snackbarOpen,
  snackBarOnClose,
  saveButtonDisabled,
  preventNavigation,
  children,
}: SettingPageTemplateProps) => {
  const classes = useStyles();
  return (
    <>
      {children}

      <Button
        id={"_saveButton"}
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

      <MessageInfo
        title={commonString.result.success}
        open={snackbarOpen}
        onClose={snackBarOnClose}
        variant={"success"}
      />

      <NavigationGuard when={preventNavigation} />
    </>
  );
};

export default SettingPageTemplate;
