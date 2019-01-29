import * as React from "react";
import {Bridge} from "../api/bridge";
import {prepLangStrings} from "../util/langstrings";
import {Button, Grid, TextField} from "@material-ui/core";
import axios, {AxiosResponse} from "axios";
import {Config} from "../config";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import {commonString} from "../util/commonstrings";
import MessageInfo from "../components/MessageInfo";

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  notice?: string,
  saved: boolean,
  deleted: boolean
}

export const strings = prepLangStrings("loginnoticepage",
  {
    title: "Login Notice Editor",
    label: "Login Notice",
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
    notice: "",
    saved: false,
    deleted: false
  };

  handleSubmitNotice = () => {
    axios.put(`${Config.baseUrl}api/loginnotice/settings`, this.state.notice)
      .then(() => this.setState({saved: true}));
  };

  handleDeleteNotice = () => {
    this.setState({notice: ""});
    axios.delete(`${Config.baseUrl}api/loginnotice/settings`)
      .then(() => this.setState({deleted: true}));
  };

  handleTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({notice: e.value});
  };

  componentDidMount = () => {
    axios
      .get(`${Config.baseUrl}api/loginnotice/settings`)
      .then((response: AxiosResponse) => {
        this.setState({notice: response.data});
      });
  };

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={strings.title}>
        <SettingsMenuContainer>
          <Grid container spacing={8} direction="column">
            <Grid item>
              <TextField id="noticeField"
                         label={strings.label}
                         rows="10"
                         variant="outlined"
                         multiline autoFocus
                         placeholder="<div></div>"
                         onChange={e => this.handleTextFieldChange(e.target)}
                         value={this.state.notice}/>
            </Grid>
            <Grid item container spacing={8} direction="row">
              <Grid item>
                <Button id="applyButton"
                        onClick={this.handleSubmitNotice}
                        variant="contained">
                  {commonString.action.apply}
                </Button>
              </Grid>
              <Grid item>
                <Button id="deleteButton"
                        disabled={this.state.notice == ""}
                        onClick={this.handleDeleteNotice}
                        variant="contained">
                  {commonString.action.delete}
                </Button>
              </Grid>
            </Grid>
          </Grid>
          <MessageInfo title={strings.notifications.saveddescription} open={this.state.saved}
                       onClose={() => this.setState({saved: false})} variant="success"/>
          <MessageInfo title={strings.notifications.deletedescription} open={this.state.deleted}
                       onClose={() => this.setState({deleted: false})} variant="success"/>
        </SettingsMenuContainer>
      </Template>
    );
  }
}

export default LoginNoticeConfigPage;
