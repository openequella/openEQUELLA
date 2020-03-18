import * as React from "react";
import {
  Theme,
  ExpansionPanel,
  ExpansionPanelSummary,
  ExpansionPanelDetails,
  Typography,
  List,
  ListItem,
  ListItemText,
  CircularProgress
} from "@material-ui/core";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../mainui/Template";
import { fetchSettings } from "./SettingsPageModule";
import { GeneralSetting } from "./SettingsPageEntry";
import { languageStrings } from "../util/langstrings";
import MUILink from "@material-ui/core/Link";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { Link } from "react-router-dom";
import { makeStyles } from "@material-ui/styles";
import AdminDownloadDialog from "../settings/AdminDownloadDialog";
import { ReactElement } from "react";
import UISettingEditor from "./UISettingEditor";
import { generateFromError } from "../api/errors";
import { AxiosError } from "axios";
import { groupMap, SettingGroup } from "./SettingGroups";

const useStyles = makeStyles((theme: Theme) => {
  return {
    heading: {
      fontSize: theme.typography.pxToRem(15),
      flexBasis: "33.33%",
      flexShrink: 0
    },
    secondaryHeading: {
      fontSize: theme.typography.pxToRem(15)
    },
    progress: {
      display: "flex",
      marginTop: theme.spacing.unit * 4,
      justifyContent: "center"
    }
  };
});

interface SettingsPageProps extends TemplateUpdateProps {
  refreshUser: () => void;
}

const SettingsPage = ({ refreshUser, updateTemplate }: SettingsPageProps) => {
  const classes = useStyles();

  const [adminDialogOpen, setAdminDialogOpen] = React.useState<boolean>(false);
  const [loading, setLoading] = React.useState<boolean>(true);
  const [settingGroups, setSettingGroups] = React.useState<SettingGroup[]>([]);

  // Update the title of App Bar
  React.useEffect(() => {
    updateTemplate(templateDefaults(languageStrings["com.equella.core"].title));
  }, []);

  // Fetch settings from the server
  React.useEffect(() => {
    // Use a flag to prevent setting state when component is being unmounted
    let cancel = false;
    fetchSettings()
      .then(settings => {
        if (!cancel) {
          setSettingGroups(groupMap(settings));
        }
      })
      .catch(error => {
        handleError(error);
      })
      .finally(() => setLoading(false));

    return () => {
      cancel = true;
    };
  }, []);

  const handleError = (error: AxiosError) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  // Create the content of each ExpansionPanel
  const expansionPanelContent = ({
    category,
    settings
  }: SettingGroup): ReactElement => {
    if (category.name === "UI") {
      return (
        <UISettingEditor refreshUser={refreshUser} handleError={handleError} />
      );
    }
    return (
      <ExpansionPanelDetails>
        <List>
          {settings.map(setting => {
            const { id, description } = setting;
            return (
              <ListItem key={id}>
                <ListItemText
                  primary={settingLink(setting)}
                  secondary={description}
                />
              </ListItem>
            );
          })}
        </List>
      </ExpansionPanelDetails>
    );
  };

  // Create a link for each setting
  const settingLink = (setting: GeneralSetting): ReactElement => {
    let link = <div />;
    if (setting.links.route) {
      link = <Link to={setting.links.route}>{setting.name}</Link>;
    } else if (setting.links.href) {
      link = <MUILink href={setting.links.href}>{setting.name}</MUILink>;
    } else if (setting.id === "adminconsole") {
      link = (
        <MUILink
          href="javascript:void(0)"
          onClick={() => setAdminDialogOpen(true)}
        >
          {setting.name}
        </MUILink>
      );
    }

    return link;
  };

  return (
    <div id="settingsPage">
      <AdminDownloadDialog
        open={adminDialogOpen}
        onClose={() => setAdminDialogOpen(false)}
      />
      {// Display a circular Progress Bar or the Setting menu, depending on the state of 'loading'
      loading ? (
        <div className={classes.progress}>
          <CircularProgress variant="indeterminate" />
        </div>
      ) : (
        settingGroups.map(group => {
          const { name, desc } = group.category;
          return (
            <ExpansionPanel key={name}>
              <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                <Typography className={classes.heading}>{name}</Typography>
                <Typography className={classes.secondaryHeading}>
                  {desc}
                </Typography>
              </ExpansionPanelSummary>
              {expansionPanelContent(group)}
            </ExpansionPanel>
          );
        })
      )}
    </div>
  );
};

export default SettingsPage;
