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
import { Tabs } from "@material-ui/core";
import Tab from "@material-ui/core/Tab";
import { NotificationType, strings } from "./LoginNoticeModule";

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  notifications: NotificationType;
  notificationOpen: boolean;
  error?: ErrorResponse;
  selectedTab: number;
}

class LoginNoticeConfigPage extends React.Component<
  LoginNoticeConfigPageProps,
  LoginNoticeConfigPageState
> {
  constructor(props: LoginNoticeConfigPageProps) {
    super(props);
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
        return strings.notifications.reverted;
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
          />
        );
      default:
        return (
          <PostLoginNoticeConfigurator
            handleError={this.handleError}
            notify={this.notify}
          />
        );
    }
  };

  render() {
    const { Template } = this.props.bridge;
    const Notifications = this.Notifications;
    const Configurators = this.Configurators;
    return (
      <Template
        title={strings.title}
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
      >
        <Configurators />
        <Notifications />
      </Template>
    );
  }
}

export default LoginNoticeConfigPage;
