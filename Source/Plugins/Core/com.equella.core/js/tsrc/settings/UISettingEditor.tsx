import * as React from "react";
import { UISetting } from "./SettingsPageEntry";
import { fetchUISetting, saveUISetting } from "./SettingsPageModule";
import { ExpansionPanelDetails, Theme } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { Link } from "react-router-dom";
import { routes } from "../mainui/routes";
import Button from "@material-ui/core/Button";
import { languageStrings } from "../util/langstrings";
import FormControl from "@material-ui/core/FormControl";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Switch from "@material-ui/core/Switch";
import { Config } from "../config";
import { AxiosError } from "axios";

const useStyles = makeStyles((theme: Theme) => {
  return {
    fab: {
      position: "absolute",
      bottom: 0,
      right: 16
    },
    enableNewUIColumn: {
      flexBasis: "33.3%"
    },
    enableNewSearchColumn: {
      flexBasis: "33.3%"
    }
  };
});

interface UISettingEditorProps {
  refreshUser: () => void;
  handleError: (error: AxiosError) => void;
}

const UISettingEditor = (props: UISettingEditorProps) => {
  const classes = useStyles();
  const { refreshUser, handleError } = props;
  const { uiconfig } = languageStrings;

  const [newUIEnabled, setNewUIEnabled] = React.useState<boolean>(true);
  const [newSearchEnabled, setNewSearchEnabled] = React.useState<boolean>(
    false
  );

  React.useEffect(() => {
    fetchUISetting()
      .then(result => {
        const setting: UISetting = result.data;
        setNewSearchEnabled(setting.newUI.newSearch);
        setNewUIEnabled(setting.newUI.enabled);
      })
      .catch(error => {
        handleError(error);
      });
  }, []);

  const setNewUI = (enabled: boolean) => {
    setNewUIEnabled(enabled);
    saveUISetting(enabled, newSearchEnabled)
      .then(_ => {
        window.location.href = Config.baseUrl + "access/settings.do";
      })
      .catch(error => {
        handleError(error);
      });
  };

  const setNewSearch = (enabled: boolean) => {
    setNewSearchEnabled(enabled);
    saveUISetting(newUIEnabled, enabled)
      .then(_ => refreshUser())
      .catch(error => {
        handleError(error);
      });
  };

  return (
    <ExpansionPanelDetails>
      <div>
        <div className={classes.enableNewUIColumn}>
          <FormControl>
            <FormControlLabel
              control={
                <Switch
                  checked={newUIEnabled}
                  onChange={(_, checked) => setNewUI(checked)}
                />
              }
              label={uiconfig.enableNew}
            />
          </FormControl>
        </div>

        <div className={classes.enableNewSearchColumn}>
          <FormControl>
            <FormControlLabel
              control={
                <Switch
                  checked={newSearchEnabled}
                  disabled={!newUIEnabled}
                  onChange={(_, checked) => setNewSearch(checked)}
                />
              }
              label={uiconfig.enableSearch}
            />
          </FormControl>
        </div>

        <Link to={routes.ThemeConfig.path}>
          <Button variant="contained" disabled={!newUIEnabled}>
            {uiconfig.themeSettingsButton}
          </Button>
        </Link>
      </div>
    </ExpansionPanelDetails>
  );
};

export default UISettingEditor;
