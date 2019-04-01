import * as React from "react";
import { Bridge } from "../api/bridge";
import { AxiosError } from "axios";
import MessageInfo from "../components/MessageInfo";
import {
  ErrorResponse,
  generateFromError,
  generateNewErrorID
} from "../api/errors";
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

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  notifications: NotificationType;
  notificationOpen: boolean;
  error?: ErrorResponse;
  selectedTab: number;
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
  LoginNoticeConfigPageProps & WithStyles<typeof styles>,
  LoginNoticeConfigPageState
> {
  private readonly postLoginNoticeConfigurator: React.RefObject<
    PostLoginNoticeConfigurator
  >;
  private readonly preLoginNoticeConfigurator: React.RefObject<
    PreLoginNoticeConfigurator
  >;

  constructor(props: LoginNoticeConfigPageProps & WithStyles<typeof styles>) {
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
    error: undefined,
    selectedTab: 0
  };

  handleError = (error: AxiosError) => {
    if (error.response != undefined) {
      switch (error.response.status) {
        case 400:
          this.setState({
            error: generateNewErrorID(strings.scheduling.endbeforestart)
          });
          return;
        case 403:
          this.setState({
            error: generateNewErrorID(strings.errors.permissions)
          });
          return;
        case 404:
          //do nothing, this simply means that there is no current login notice
          return;
      }
    }
    this.setState({ error: generateFromError(error) });
  };

  handleChangeTab = (event: React.ChangeEvent<{}>, value: number) => {
    this.setState({ selectedTab: value });
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
          />
        );
      default:
        return (
          <PostLoginNoticeConfigurator
            handleError={this.handleError}
            notify={this.notify}
            ref={this.postLoginNoticeConfigurator}
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

  render() {
    const { Template, routes } = this.props.bridge;
    const { classes } = this.props;
    const Notifications = this.Notifications;
    const Configurators = this.Configurators;
    return (
      <Template
        title={strings.title}
        backRoute={routes.SettingsPage}
        fixedViewPort
        tabs={
          <Tabs
            value={this.state.selectedTab}
            onChange={this.handleChangeTab}
            variant="fullWidth"
          >
            <Tab id="preTab" label={strings.prelogin.label} />
            <Tab id="postTab" label={strings.postlogin.label} />
          </Tabs>
        }
        errorResponse={this.state.error}
        footer={
          <Button
            id="SaveButton"
            className={classes.floatingButton}
            onClick={this.handleSubmitButton}
            variant="contained"
            size="large"
          >
            {commonString.action.save}
          </Button>
        }
      >
        <Configurators />
        <Notifications />
      </Template>
    );
  }
}

export default withStyles(styles)(LoginNoticeConfigPage);
