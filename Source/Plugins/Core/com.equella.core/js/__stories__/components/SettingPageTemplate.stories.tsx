import * as React from "react";
import { action } from "@storybook/addon-actions";
import SettingPageTemplate from "../../tsrc/components/SettingPageTemplate";
import { Card } from "@material-ui/core";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: "SettingPageTemplate",
  component: SettingPageTemplate,
};

const children = (
  <Card>
    <h3>SettingPageTemplate Story</h3>
  </Card>
);

export const saveButtonEnabled = () => (
  <SettingPageTemplate
    onSave={action("save")}
    saveButtonDisabled={boolean("disable Save butto", false)}
    snackbarOpen={boolean("open snack bar", false)}
    preventNavigation={boolean("prevent navigation", true)}
    snackBarOnClose={action("Close snackbar")}
  >
    {children}
  </SettingPageTemplate>
);
