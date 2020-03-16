import * as React from "react";
import { Theme } from "@material-ui/core";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { fetchSettings } from "./SettingsPageModule";
import { GeneralSetting } from "./SettingsPageEntry";
import { languageStrings } from "../util/langstrings";
import ExpansionPanel from "@material-ui/core/ExpansionPanel";
import ExpansionPanelSummary from "@material-ui/core/ExpansionPanelSummary";
import ExpansionPanelDetails from "@material-ui/core/ExpansionPanelDetails";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import Typography from "@material-ui/core/Typography";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemText from "@material-ui/core/ListItemText";
import { Link } from "react-router-dom";
import { makeStyles } from "@material-ui/styles";
import AdminDownloadDialog from "../settings/AdminDownloadDialog";
import { ReactElement } from "react";
import UISettingEditor from "./UISettingEditor";
import CircularProgress from "@material-ui/core/CircularProgress";

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

interface SettingCategory {
  id: string;
  details: {
    name: string;
    desc: string;
  };
}

interface SettingGroup {
  category: SettingCategory;
  settings: GeneralSetting[];
}

const SettingsPage = (props: SettingsPageProps) => {
  const classes = useStyles();
  const { refreshUser, updateTemplate } = props;
  const SETTING_CATEGORIES: SettingCategory[] = [
    { id: "general", details: languageStrings.settings.general },
    { id: "integration", details: languageStrings.settings.integration },
    { id: "diagnostics", details: languageStrings.settings.diagnostics },
    { id: "searching", details: languageStrings.settings.searching },
    { id: "ui", details: languageStrings.settings.ui }
  ];

  const [settings, setSettings] = React.useState<GeneralSetting[]>([]);
  const [adminDialogOpen, setAdminDialogOpen] = React.useState<boolean>(false);
  const [loading, setLoading] = React.useState<boolean>(true);

  // Update the title of App Bar
  React.useEffect(() => {
    updateTemplate(templateDefaults(languageStrings["com.equella.core"].title));
  }, []);

  // Fetch settings from the server
  React.useEffect(() => {
    let cancel = false;
    fetchSettings().then(result => {
      if (!cancel) {
        setSettings(result.data);
        setLoading(false);
      }
    });
    return () => {
      cancel = true;
    };
  }, []);

  // Group settings by their category and sort each group by setting name
  const settingGroups = (): SettingGroup[] => {
    const groups: SettingGroup[] = [];

    SETTING_CATEGORIES.forEach(category => {
      const settingsOfCategory = settings
        .filter(setting => setting.group === category.id)
        .sort((s1, s2) => {
          return s1.name > s2.name ? 1 : -1;
        });
      groups.push({ category: category, settings: settingsOfCategory });
    });

    return groups;
  };

  // Create the content of each ExpansionPanel
  const expansionPanelContent = (
    category: string,
    settings?: GeneralSetting[]
  ): ReactElement => {
    if (category === "ui") {
      return <UISettingEditor refreshUser={refreshUser} />;
    } else {
      return (
        <ExpansionPanelDetails>
          <List>
            {settings &&
              settings.map(setting => {
                return (
                  <ListItem key={setting.id}>
                    <ListItemText
                      primary={settingLink(setting)}
                      secondary={setting.description}
                    />
                  </ListItem>
                );
              })}
          </List>
        </ExpansionPanelDetails>
      );
    }
  };

  // Create a link for each setting
  const settingLink = (setting: GeneralSetting): ReactElement => {
    let link = <div />;
    if (setting.links.route) {
      link = <Link to={setting.links.route}>{setting.name}</Link>;
    } else if (setting.links.href) {
      link = <a href={setting.links.href}>{setting.name}</a>;
    } else if (setting.id === "adminconsole") {
      link = (
        <a href="javascript:void(0)" onClick={() => setAdminDialogOpen(true)}>
          {setting.name}
        </a>
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
        settingGroups().map(group => {
          const { category } = group;
          return (
            <ExpansionPanel key={category.id}>
              <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                <Typography className={classes.heading}>
                  {category.details.name}
                </Typography>
                <Typography className={classes.secondaryHeading}>
                  {category.details.desc}
                </Typography>
              </ExpansionPanelSummary>
              {expansionPanelContent(category.id, group.settings)}
            </ExpansionPanel>
          );
        })
      )}
    </div>
  );
};

export default SettingsPage;
