import * as React from "react";
import { Button, Grid, Typography } from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  strings,
  submitPreLoginNotice,
  uploadPreLoginNoticeImage,
  emptyTinyMCEString
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import RichTextEditor from "../components/RichTextEditor";
import SettingsMenuContainer from "../components/SettingsMenuContainer";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PreLoginNoticeConfiguratorState {
  html: string;
  dbHtml: string;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      html: "",
      dbHtml: ""
    };
  }

  handleSubmitPreNotice = () => {
    if (this.state.html == emptyTinyMCEString) {
      clearPreLoginNotice()
        .then(() => {
          this.props.notify(NotificationType.Clear);
          this.setState({
            dbHtml: this.state.html
          });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    } else {
      submitPreLoginNotice(this.state.html)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setState({
            dbHtml: this.state.html
          });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleUndoPreNotice = () => {
    this.setState(
      {
        //swap the states to force an update
        html: this.state.dbHtml,
        dbHtml: this.state.html
      },
      () => this.setState({ dbHtml: this.state.html })
    ); //set the dbHtml back to it's original value to update the editor
    this.props.notify(NotificationType.Revert);
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({
          dbHtml: response.data,
          html: response.data
        });
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleEditorChange = (html: string) => {
    this.setState({ html: html });
  };

  render() {
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subtitle1">
          {strings.prelogin.label}
        </Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <RichTextEditor
              htmlInput={this.state.dbHtml}
              onStateChange={this.handleEditorChange}
              imageUploadCallBack={uploadPreLoginNoticeImage}
            />
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button
                id="preApplyButton"
                onClick={this.handleSubmitPreNotice}
                variant="contained"
              >
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
                onClick={this.handleUndoPreNotice}
                variant="text"
                disabled={this.state.html == this.state.dbHtml}
              >
                {commonString.action.cancel}
              </Button>
            </Grid>
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
