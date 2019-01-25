import * as React from "react";
import {Bridge} from "../api/bridge";
import {prepLangStrings} from "../util/langstrings";
import {Button, Grid, TextField} from "@material-ui/core";
import axios, {AxiosResponse} from "axios";
import {Config} from "../config";
import SettingsMenuContainer from "../components/SettingsMenuContainer";

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
}

interface LoginNoticeConfigPageState {
  notice?: string
}

export const strings = prepLangStrings("loginnoticepage",
  {
    title: "Login Notice Editor",
    label: "Login Notice"
  }
);

class LoginNoticeConfigPage extends React.Component<LoginNoticeConfigPageProps, LoginNoticeConfigPageState> {

  constructor(props:LoginNoticeConfigPageProps) {
    super(props);
  };

  state: LoginNoticeConfigPageState = {
    notice: ""
  };

  handleSubmitNotice = () => {
    axios.put(`${Config.baseUrl}api/loginnotice/settings/`, this.state.notice);
  };

  handleTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({notice: e.value});
  };

  componentDidMount = () => {
    axios
      .get(`${Config.baseUrl}api/loginnotice/settings/`)
      .then((response: AxiosResponse) =>
      {
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
                           rows={10}
                           variant="outlined"
                           multiline autoFocus
                           placeholder="<div></div>"
                           onChange={e => this.handleTextFieldChange(e.target)}
                           value={this.state.notice}/>
              </Grid>
              <Grid item>
                <Button id="applyButton"
                        onClick={this.handleSubmitNotice}
                        variant="contained">
                  Apply
                </Button>
              </Grid>
            </Grid>
        </SettingsMenuContainer>
      </Template>
    );
  }
}
export default LoginNoticeConfigPage;
