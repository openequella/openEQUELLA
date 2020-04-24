import * as React from "react";
import {
  templateDefaults,
  TemplateUpdate,
  TemplateUpdateProps,
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Button, Card, Mark, Slider } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../util/langstrings";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings,
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
    width: "calc(25% - 112px)",
  },
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left",
  },
});

function ContentIndexSettings({ updateTemplate }: TemplateUpdateProps) {
  const [searchSettings, setSearchSettings] = React.useState<SearchSettings>(
    defaultSearchSettings
  );
  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;

  const markStrings = contentIndexSettingsStrings.sliderMarks;
  const classes = useStyles();

  const boostVals: Mark[] = [
    { label: markStrings.off, value: 0 },
    { label: "x0.25", value: 1 },
    { label: "x0.5", value: 2 },
    { label: markStrings.noBoost, value: 3 },
    { label: "x1.5", value: 4 },
    { label: "x2", value: 5 },
    { label: "x4", value: 6 },
    { label: "x8", value: 7 },
  ];

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(contentIndexSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
    getSearchSettingsFromServer()
      .then((settings: SearchSettings) => setSearchSettings(settings))
      .catch((error) => handleError(error));
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

  const handleSliderChange = (newValue: number, prop: string) => {
    setSearchSettings({
      ...searchSettings,
      [prop]: newValue,
    });
  };

  const getAriaLabel = (value: number, index: number): string => {
    return boostVals[value].label as string;
  };

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
                setValue={(level) =>
                  setSearchSettings({
                    ...searchSettings,
                    urlLevel: level,
                  })
                }
              />
            }
          />
        </SettingsList>
      </Card>
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={contentIndexSettingsStrings.boosting}>
          <SettingsListControl
            primaryText={contentIndexSettingsStrings.titleBoostingTitle}
            divider
            control={
              <Slider
                disabled={showError}
                marks={boostVals}
                min={0}
                max={7}
                getAriaValueText={getAriaLabel}
                aria-label={contentIndexSettingsStrings.titleBoostingTitle}
                value={searchSettings.titleBoost}
                onChange={(event, value) =>
                  handleSliderChange(value as number, "titleBoost")
                }
              />
            }
          />
          <SettingsListControl
            primaryText={contentIndexSettingsStrings.metaBoostingTitle}
            divider
            control={
              <Slider
                disabled={showError}
                marks={boostVals}
                min={0}
                max={7}
                getAriaValueText={getAriaLabel}
                aria-label={contentIndexSettingsStrings.metaBoostingTitle}
                value={searchSettings.descriptionBoost}
                onChange={(event, value) =>
                  handleSliderChange(value as number, "descriptionBoost")
                }
              />
            }
          />
          <SettingsListControl
            primaryText={contentIndexSettingsStrings.attachmentBoostingTitle}
            control={
              <Slider
                disabled={showError}
                marks={boostVals}
                min={0}
                max={7}
                getAriaValueText={getAriaLabel}
                aria-label={contentIndexSettingsStrings.attachmentBoostingTitle}
                value={searchSettings.attachmentBoost}
                onChange={(event, value) =>
                  handleSliderChange(value as number, "attachmentBoost")
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
