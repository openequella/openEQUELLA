import * as React from "react";
import { AxiosError } from "axios";
import MessageInfo from "../components/MessageInfo";
import { generateFromError, generateNewErrorID } from "../api/errors";
import PreLoginNoticeConfigurator from "./PreLoginNoticeConfigurator";
import PostLoginNoticeConfigurator from "./PostLoginNoticeConfigurator";
import {
  Button,
  createStyles,
  Tabs,
  Theme,
  withStyles,
  WithStyles
} from "@material-ui/core";
import Tab from "@material-ui/core/Tab";
import { NotificationType, strings } from "./LoginNoticeModule";
import { commonString } from "../util/commonstrings";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../mainui/Template";
import { routes } from "../mainui/routes";

interface LoginNoticeConfigPageProps
  extends TemplateUpdateProps,
    WithStyles<typeof styles> {
  setPreventNavigation(b: boolean): void;
}

interface LoginNoticeConfigPageState {
  notifications: NotificationType;
  notificationOpen: boolean;
  selectedTab: number;
  preventNav: boolean;
}

const styles = (theme: Theme) =>
  createStyles({
    floatingButton: {
      right: theme.spacing.unit * 2,
      bottom: theme.spacing.unit * 2,
      position: "fixed"
    }
  });

class LoginNoticeConfigPage extends React.Component<
  LoginNoticeConfigPageProps,
  LoginNoticeConfigPageState
> {
  private readonly postLoginNoticeConfigurator: React.RefObject<
    PostLoginNoticeConfigurator
  >;
  private readonly preLoginNoticeConfigurator: React.RefObject<
    PreLoginNoticeConfigurator
  >;

  constructor(props: LoginNoticeConfigPageProps) {
    super(props);
    this.preLoginNoticeConfigurator = React.createRef<
      PreLoginNoticeConfigurator
    >();
    this.postLoginNoticeConfigurator = React.createRef<
      PostLoginNoticeConfigurator
    >();
  }

  state: LoginNoticeConfigPageState = {
    notifications: NotificationType.Save,
    notificationOpen: false,
    selectedTab: 0,
    preventNav: false
  };

  componentDidMount() {
    const { classes, updateTemplate } = this.props;
    updateTemplate(tp => ({
      ...templateDefaults(strings.title)(tp),
      backRoute: routes.Settings.to,
      fixedViewPort: true,
      footer: (
        <Button
          id="SaveButton"
          className={classes.floatingButton}
          onClick={this.handleSubmitButton}
          variant="contained"
          size="large"
        >
          {commonString.action.save}
        </Button>
      ),
      tabs: this.tabs()
    }));
  }

  handleError = (error: AxiosError) => {
    var errResponse;
    if (error.response != undefined) {
      switch (error.response.status) {
        case 400:
          errResponse = generateNewErrorID(strings.scheduling.endbeforestart);
          break;
        case 403:
          errResponse = generateNewErrorID(strings.errors.permissions);
          break;
        case 404:
          //do nothing, this simply means that there is no current login notice
          return;
      }
    } else {
      errResponse = generateFromError(error);
    }
    if (errResponse) {
      this.props.updateTemplate(templateError(errResponse));
    }
  };

  handleChangeTab = (event: React.ChangeEvent<{}>, selectedTab: number) => {
    this.props.updateTemplate(tp => ({ ...tp, tabs: this.tabs() }));
    this.setState({ selectedTab });
  };

  clearNotifications = () => {
    this.setState({ notificationOpen: false });
  };

  notificationString = (notificationType: NotificationType): string => {
    switch (notificationType) {
      case NotificationType.Revert:
        return strings.notifications.cancelled;
      case NotificationType.Clear:
        return strings.notifications.cleared;
      case NotificationType.Save:
        return strings.notifications.saved;
    }
  };

  Notifications = () => {
    return (
      <MessageInfo
        title={this.notificationString(this.state.notifications)}
        open={this.state.notificationOpen}
        onClose={this.clearNotifications}
        variant="success"
      />
    );
  };

  notify = (notificationType: NotificationType) => {
    this.setState({ notificationOpen: true, notifications: notificationType });
  };

  Configurators = () => {
    switch (this.state.selectedTab) {
      case 0:
        return (
          <PreLoginNoticeConfigurator
            handleError={this.handleError}
            notify={this.notify}
            ref={this.preLoginNoticeConfigurator}
            preventNav={this.preventNav}
          />
        );
      default:
        return (
          <PostLoginNoticeConfigurator
            handleError={this.handleError}
            notify={this.notify}
            ref={this.postLoginNoticeConfigurator}
            preventNav={this.preventNav}
          />
        );
    }
  };

  handleSubmitButton = () => {
    switch (this.state.selectedTab) {
      case 0:
        if (this.preLoginNoticeConfigurator.current) {
          this.preLoginNoticeConfigurator.current.handleSubmitPreNotice();
        }
        break;
      default:
        if (this.postLoginNoticeConfigurator.current) {
          this.postLoginNoticeConfigurator.current.handleSubmitPostNotice();
        }
        break;
    }
  };

  preventNav = (preventNav: boolean) => {
    this.setState({ preventNav }, () =>
      this.props.setPreventNavigation(preventNav)
    );
  };

  tabs = () => (
    <Tabs
      value={this.state.selectedTab}
      onChange={this.handleChangeTab}
      variant="fullWidth"
    >
      <Tab
        id="preTab"
        label={strings.prelogin.label}
        disabled={this.state.preventNav}
      />
      <Tab
        id="postTab"
        label={strings.postlogin.label}
        disabled={this.state.preventNav}
      />
    </Tabs>
  );

  render() {
    const Notifications = this.Notifications;
    const Configurators = this.Configurators;
    return (
      <React.Fragment>
        <Configurators />
        <Notifications />
      </React.Fragment>
    );
  }
}

export default withStyles(styles)(LoginNoticeConfigPage);
