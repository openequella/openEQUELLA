import * as React from "react";
import {Bridge} from "../api/bridge";
import {prepLangStrings} from "../util/langstrings";
import {AxiosError} from "axios";
import MessageInfo from "../components/MessageInfo";
import {ErrorResponse, generateFromAxiosError} from "../api/errors";
import PreLoginNoticeConfigurator from "./PreLoginNoticeConfigurator";
import PostLoginNoticeConfigurator from "./PostLoginNoticeConfigurator";
import {Tabs} from "@material-ui/core";
import Tab from "@material-ui/core/Tab";

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  saved: boolean,
  deleted: boolean,
  undone: boolean,
  error?: ErrorResponse,
  selectedTab: number
}

export const strings = prepLangStrings("loginnoticepage",
  {
    title: "Login Notice Editor",
    currentnotice: "Current Notice: ",
    prelogin: {
      label: "Before Login Notice",
      description: "Write a plaintext message to be displayed on the login screen..."
    },
    postlogin: {
      label: "After Login Notice",
      description: "Write a plaintext message to be displayed after login as an alert..."
    },
    notifications: {
      saveddescription: "Login notice saved successfully.",
      deletedescription: "Login notice deleted successfully.",
      undodescription: "Reverted changes to login notice."
    }
  }
);

class LoginNoticeConfigPage extends React.Component<LoginNoticeConfigPageProps, LoginNoticeConfigPageState> {

  constructor(props: LoginNoticeConfigPageProps) {
    super(props);
  };

  state: LoginNoticeConfigPageState = {
    saved: false,
    deleted: false,
    undone: false,
    error: undefined,
    selectedTab: 0
  };

  handleError = (axiosError: AxiosError) => {
    if (axiosError.response != undefined) {
      switch (axiosError.response.status) {
        case 404:
          //do nothing, this simply means that there is no current login notice
          break;
        default:
          this.setState({error: generateFromAxiosError(axiosError)});
          break;
      }
    }
  };

  handleChange = (event: React.ChangeEvent<{}>, value: number) => {
    this.setState({selectedTab: value})
  };

  Notifications = () => {
    return (
      <div>
        <MessageInfo title={strings.notifications.saveddescription} open={this.state.saved}
                     onClose={() => this.setState({saved: false})} variant="success"/>
        <MessageInfo title={strings.notifications.deletedescription} open={this.state.deleted}
                     onClose={() => this.setState({deleted: false})} variant="success"/>
        <MessageInfo title={strings.notifications.undodescription} open={this.state.undone}
                     onClose={() => this.setState({undone: false})} variant="info"/>
      </div>
    );
  };

  render() {
    const {Template} = this.props.bridge;
    const Notifications = this.Notifications;
    return (
      <Template title={strings.title}
                tabs={
                  <Tabs value={this.state.selectedTab} onChange={this.handleChange} fullWidth>
                    <Tab label={strings.prelogin.label}/>
                    <Tab label={strings.postlogin.label}/>
                  </Tabs>}
                errorResponse={this.state.error || undefined}
      >
        {this.state.selectedTab == 0 &&
        <PreLoginNoticeConfigurator handleError={this.handleError} onSaved={() => this.setState({saved: true})}
                                    onDeleted={() => this.setState({deleted: true})}
                                    onUndone={() => this.setState({undone: true})}/>}
        {this.state.selectedTab == 1 &&
        <PostLoginNoticeConfigurator handleError={this.handleError} onSaved={() => this.setState({saved: true})}
                                     onDeleted={() => this.setState({deleted: true})}
                                     onUndone={() => this.setState({undone: true})}/>}
        <Notifications/>
      </Template>
    );
  }
}

export default LoginNoticeConfigPage;
