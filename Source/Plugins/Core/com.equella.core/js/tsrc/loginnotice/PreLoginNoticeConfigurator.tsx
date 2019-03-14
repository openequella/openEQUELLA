import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
  Typography
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  strings,
  submitPreLoginNotice,
  uploadPreLoginNoticeImage
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
  clearStaged: boolean;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      html: "",
      dbHtml: "",
      clearStaged: false
    };
  }

  handleSubmitPreNotice = () => {
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
  };

  forceEditorRefresh = () => {
    this.setState({
      html: this.state.dbHtml,
      dbHtml: this.state.html
    });
  };

  handleClearPreNotice = () => {
    clearPreLoginNotice()
      .then(() => {
        this.forceEditorRefresh();
        this.setState({
          clearStaged: false,
          html: "",
          dbHtml: ""
        });
        this.props.notify(NotificationType.Clear);
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
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
        console.log(error);
        this.props.handleError(error);
      });
  };

  stageClear = () => {
    this.setState({ clearStaged: true });
  };

  Dialogs = () => {
    return (
      <div>
        <Dialog
          open={this.state.clearStaged}
          onClose={() => this.setState({ clearStaged: false })}
        >
          <DialogTitle>{strings.clear.title}</DialogTitle>
          <DialogContent>
            <DialogContentText>{strings.clear.confirm}</DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button id="okToClear" onClick={this.handleClearPreNotice}>
              {commonString.action.ok}
            </Button>
            <Button
              id="cancelClear"
              onClick={() => this.setState({ clearStaged: false })}
            >
              {commonString.action.cancel}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  };

  handleEditorChange = (html: string) => {
    this.setState({ html: html });
  };

  render() {
    const Dialogs = this.Dialogs;
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
              imageUploadCallBack={(file: object) =>
                uploadPreLoginNoticeImage(file).catch((error: AxiosError) =>
                  this.props.handleError(error)
                )
              }
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
                id="preClearButton"
                onClick={this.stageClear}
                variant="text"
                disabled={this.state.html == ""}
              >
                {commonString.action.clear}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
                onClick={this.handleUndoPreNotice}
                variant="text"
                disabled={this.state.html == this.state.dbHtml}
              >
                {commonString.action.revertchanges}
              </Button>
            </Grid>
          </Grid>
        </Grid>
        <Dialogs />
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
