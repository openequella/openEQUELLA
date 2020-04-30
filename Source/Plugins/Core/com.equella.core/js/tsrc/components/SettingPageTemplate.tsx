import * as React from "react";
import { ReactNode } from "react";
import { commonString } from "../util/commonstrings";
import MessageInfo from "./MessageInfo";
import { NavigationGuard } from "./NavigationGuard";
import SaveButton from "./SaveButton";

interface SettingPageTemplateProps {
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
  return (
    <>
      {children}
      <SaveButton onSave={onSave} saveButtonDisabled={saveButtonDisabled} />

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
