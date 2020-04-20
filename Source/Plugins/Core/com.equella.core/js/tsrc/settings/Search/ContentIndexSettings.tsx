import * as React from "react";
import {
  templateDefaults,
  TemplateUpdate,
  TemplateUpdateProps
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Button, Card } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../util/langstrings";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings
} from "./SearchSettingsModule";
import MessageInfo from "../../components/MessageInfo";
import { Save } from "@material-ui/icons";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import WebPageIndexSetting from "./components/WebPageIndexSetting";

const useStyles = makeStyles({
  floatingButton: {
    position: "fixed",
    top: 0,
    right: 0,
    marginTop: "80px",
    marginRight: "16px",
    width: "calc(25% - 112px)"
  },
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left"
  }
});

function ContentIndexSettings({ updateTemplate }: TemplateUpdateProps) {
  const [searchSettings, setSearchSettings] = React.useState<SearchSettings>(
    defaultSearchSettings
  );
  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;
  const classes = useStyles();

  React.useEffect(() => {
    updateTemplate(tp => ({
      ...templateDefaults(contentIndexSettingsStrings.name)(tp),
      backRoute: routes.Settings.to
    }));
    getSearchSettingsFromServer()
      .then((settings: SearchSettings) => setSearchSettings(settings))
      .catch(error => handleError(error));
  }, []);

  function handleError(error: TemplateUpdate) {
    setShowError(true);
    updateTemplate(error);
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => setShowSuccess(true))
      .catch((error: TemplateUpdate) => handleError(error));
  }

  return (
    <>
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={contentIndexSettingsStrings.general}>
          <SettingsListControl
            primaryText={contentIndexSettingsStrings.name}
            secondaryText={contentIndexSettingsStrings.description}
            control={
              <WebPageIndexSetting
                disabled={showError}
                value={searchSettings.urlLevel}
                setValue={level =>
                  setSearchSettings({
                    ...searchSettings,
                    urlLevel: level
                  })
                }
              />
            }
          />
        </SettingsList>
      </Card>
      {/*Save Button*/}
      <Button
        color={"primary"}
        id={"_saveButton"}
        disabled={showError}
        className={classes.floatingButton}
        variant="contained"
        onClick={handleSubmitButton}
        size="large"
      >
        <Save />
        {contentIndexSettingsStrings.save}
      </Button>
      {/*Snackbar*/}
      <MessageInfo
        title={contentIndexSettingsStrings.success}
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        variant={"success"}
      />
    </>
  );
}

export default ContentIndexSettings;
