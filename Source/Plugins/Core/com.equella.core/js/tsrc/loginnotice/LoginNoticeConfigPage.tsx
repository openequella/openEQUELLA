import * as React from "react";
import {Bridge} from "../api/bridge";
import {prepLangStrings} from "../util/langstrings";
import {AxiosError} from "axios";
import MessageInfo from "../components/MessageInfo";
import {ErrorResponse, generateFromAxiosError} from "../api/errors";
import PreLoginNoticeConfigurator from "./PreLoginNoticeConfigurator";
import PostLoginNoticeConfigurator from "./PostLoginNoticeConfigurator";

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  preNotice?: string,
  postNotice?: string,
  saved: boolean,
  deleted: boolean,
  error?: ErrorResponse
}

export const strings = prepLangStrings("loginnoticepage",
  {
    title: "Login Notice Editor",
    prelogin: {
      label: "Before Login Notice",
    },
    postlogin: {
      label: "After Login Notice",
    },
    notifications: {
      saveddescription: "Login notice saved successfully.",
      deletedescription: "Login notice deleted successfully."
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
    error: undefined
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

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={strings.title} errorResponse={this.state.error || undefined}>
        <PreLoginNoticeConfigurator handleError={this.handleError} onSaved={() =>this.setState({saved:true})} onDeleted={() =>this.setState({deleted:true})}/>
        <PostLoginNoticeConfigurator handleError={this.handleError} onSaved={() =>this.setState({saved:true})} onDeleted={() =>this.setState({deleted:true})}/>
          <MessageInfo title={strings.notifications.saveddescription} open={this.state.saved}
                       onClose={() => this.setState({saved: false})} variant="success"/>
          <MessageInfo title={strings.notifications.deletedescription} open={this.state.deleted}
                       onClose={() => this.setState({deleted: false})} variant="success"/>
      </Template>
    );
  }
}

export default LoginNoticeConfigPage;
