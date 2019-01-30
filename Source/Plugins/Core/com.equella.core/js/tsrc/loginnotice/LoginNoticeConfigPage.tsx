import * as React from "react";
import {Bridge} from "../api/bridge";
import {prepLangStrings} from "../util/langstrings";
import {Button, Grid, TextField} from "@material-ui/core";
import {AxiosError, AxiosResponse} from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import {commonString} from "../util/commonstrings";
import MessageInfo from "../components/MessageInfo";
import {ErrorResponse, generateFromAxiosError} from "../api/errors";
import {
  deletePostLoginNotice,
  deletePreLoginNotice,
  getPostLoginNotice,
  getPreLoginNotice,
  submitPostLoginNotice,
  submitPreLoginNotice
} from "./LoginNoticeModule";


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
    preNotice: "",
    postNotice: "",
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

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      submitPreLoginNotice(this.state.preNotice)
        .then(() => this.setState({saved: true}))
        .catch((error) => {
          this.handleError(error);
        });
    }
  };

  handleSubmitPostNotice = () => {
    if (this.state.postNotice != undefined) {
      submitPostLoginNotice(this.state.postNotice)
        .then(() => this.setState({saved: true}))
        .catch((error) => {
          this.handleError(error);
        });
    }
  };

  handleDeletePreNotice = () => {
    this.setState({preNotice: ""});
    deletePreLoginNotice()
      .then(() => this.setState({deleted: true}))
      .catch((error) => {
        this.handleError(error);
      });
  };

  handleDeletePostNotice = () => {
    this.setState({postNotice: ""});
    deletePostLoginNotice()
      .then(() => this.setState({deleted: true}))
      .catch((error) => {
        this.handleError(error);
      });
  };

  handlePreTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({preNotice: e.value});
  };

  handlePostTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({postNotice: e.value});
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({preNotice: response.data});
      })
      .catch((error) => {
        this.handleError(error);
      });
    getPostLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({postNotice: response.data});
      })
      .catch((error) => {
        this.handleError(error);
      });
  };

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={strings.title} errorResponse={this.state.error || undefined}>
        <SettingsMenuContainer>
          <Grid container spacing={8} direction="column">
            <Grid item>
              <TextField id="noticeField"
                         label={strings.prelogin.label}
                         rows="10"
                         variant="outlined"
                         multiline autoFocus
                         placeholder="<div></div>"
                         onChange={e => this.handlePreTextFieldChange(e.target)}
                         value={this.state.preNotice}/>
            </Grid>
            <Grid item container spacing={8} direction="row">
              <Grid item>
                <Button id="applyButton"
                        onClick={this.handleSubmitPreNotice}
                        variant="contained">
                  {commonString.action.apply}
                </Button>
              </Grid>
              <Grid item>
                <Button id="deleteButton"
                        disabled={this.state.preNotice == ""}
                        onClick={this.handleDeletePreNotice}
                        variant="contained">
                  {commonString.action.delete}
                </Button>
              </Grid>
            </Grid>
          </Grid>
        </SettingsMenuContainer>
        <SettingsMenuContainer>
          <Grid container spacing={8} direction="column">
            <Grid item>
              <TextField id="postNoticeField"
                         label={strings.postlogin.label}
                         rows="10"
                         variant="outlined"
                         multiline
                         placeholder="<div></div>"
                         onChange={e => this.handlePostTextFieldChange(e.target)}
                         value={this.state.postNotice}/>
            </Grid>
            <Grid item container spacing={8} direction="row">
              <Grid item>
                <Button id="applyButton"
                        onClick={this.handleSubmitPostNotice}
                        variant="contained">
                  {commonString.action.apply}
                </Button>
              </Grid>
              <Grid item>
                <Button id="deleteButton"
                        disabled={this.state.postNotice == ""}
                        onClick={this.handleDeletePostNotice}
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
