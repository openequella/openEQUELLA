import * as React from "react";
import { fetchUISetting, saveUISetting } from "./SettingsPageModule";
import {
  ExpansionPanelDetails,
  Theme,
  Button,
  FormControl,
  FormControlLabel,
  Switch,
  Grid
} from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { Link } from "react-router-dom";
import { routes } from "../mainui/routes";
import { languageStrings } from "../util/langstrings";
import { Config } from "../config";

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
  handleError: (error: Error) => void;
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
      .then(uiSetting => {
        const { enabled, newSearch } = uiSetting.newUI;
        setNewUIEnabled(enabled);
        setNewSearchEnabled(newSearch);
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
      <Grid container direction={"column"}>
        <Grid item>
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
        </Grid>

        <Grid item>
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
        </Grid>

        <Grid item>
          <Link to={routes.ThemeConfig.path}>
            <Button variant="contained" disabled={!newUIEnabled}>
              {uiconfig.themeSettingsButton}
            </Button>
          </Link>
        </Grid>
      </Grid>
    </ExpansionPanelDetails>
  );
};

export default UISettingEditor;
