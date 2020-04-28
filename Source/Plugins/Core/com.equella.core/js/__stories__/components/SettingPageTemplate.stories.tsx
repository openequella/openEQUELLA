import * as React from "react";
import { action } from "@storybook/addon-actions";
import SettingPageTemplate from "../../tsrc/components/SettingPageTemplate";
import { Card } from "@material-ui/core";

export default {
  title: "SettingPageTemplate",
  component: SettingPageTemplate
};

const onClose = action("Close snackbar");
const onSave = action("save");
const children = (
  <Card>
    <h3>SettingPageTemplate Story</h3>
  </Card>
);

export const saveButtonEnabled = () => (
  <SettingPageTemplate
    onSave={onSave}
    saveButtonDisabled={false}
    snackbarOpen={false}
    preventNavigation
    snackBarOnClose={onClose}
  >
    {children}
  </SettingPageTemplate>
);

export const snackbarDisplayed = () => (
  <SettingPageTemplate
    onSave={onSave}
    saveButtonDisabled={true}
    snackbarOpen={true}
    preventNavigation
    snackBarOnClose={onClose}
  >
    {children}
  </SettingPageTemplate>
);
