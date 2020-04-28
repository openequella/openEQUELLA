import * as React from "react";
import { useEffect, useState } from "react";
import { Prompt, useHistory } from "react-router";
import ConfirmDialog from "./ConfirmDialog";
import { Location } from "history";
import { commonString } from "../util/commonstrings";
import { languageStrings } from "../util/langstrings";

interface Props {
  /**
   * A condition when navigation should be prevented.
   */
  when: boolean;
}

const NavigationGuard = ({ when }: Props) => {
  const history = useHistory();
  const { message, title } = languageStrings.navigationguard;

  const [showPrompt, setShowPrompt] = useState(false);
  const [confirmedNavigation, setConfirmedNavigation] = useState(false);
  const [navigateTo, setNavigateTo] = useState<Location>();

  /**
   * Navigate to other pages, depending on if user confirms and next location.
   */
  useEffect(() => {
    if (confirmedNavigation && navigateTo) {
      history.push(navigateTo.pathname);
    }
  }, [confirmedNavigation, navigateTo]);

  /**
   * Show a dialog to let user confirm and save the next location.
   * If user confirms then don't block the navigation and hide the dialog.
   */
  const blockNavigation = (location: Location): boolean => {
    if (confirmedNavigation) {
      return true;
    }
    setNavigateTo(location);
    setShowPrompt(true);
    return false;
  };

  const confirmNavigation = () => {
    setConfirmedNavigation(true);
    setShowPrompt(false);
  };

  const cancelNavigation = () => {
    setShowPrompt(false);
  };

  return (
    <>
      <Prompt when={when} message={blockNavigation} />
      <ConfirmDialog
        title={title}
        onConfirm={confirmNavigation}
        onCancel={cancelNavigation}
        open={showPrompt}
        confirmButtonText={commonString.action.ok}
      >
        {message}
      </ConfirmDialog>
    </>
  );
};

export default NavigationGuard;
