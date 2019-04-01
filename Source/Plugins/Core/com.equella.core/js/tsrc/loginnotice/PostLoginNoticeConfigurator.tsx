import * as React from "react";
import {
  Button,
  DialogContent,
  DialogContentText,
  Grid,
  TextField
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPostLoginNotice,
  getPostLoginNotice,
  NotificationType,
  submitPostLoginNotice,
  strings
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";

interface PostLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PostLoginNoticeConfiguratorState {
  postNotice?: string; //what is currently in the textfield
  dbPostNotice?: string; //what is currently in the database
  clearStaged: boolean;
}

class PostLoginNoticeConfigurator extends React.Component<
  PostLoginNoticeConfiguratorProps,
  PostLoginNoticeConfiguratorState
> {
  constructor(props: PostLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      postNotice: "",
      dbPostNotice: "",
      clearStaged: false
    };
  }

  handleSubmitPostNotice = () => {
    if (this.state.postNotice != undefined) {
      submitPostLoginNotice(this.state.postNotice)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setState({ dbPostNotice: this.state.postNotice });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleClearPostNotice = () => {
    this.setState({ postNotice: "" });
    clearPostLoginNotice()
      .then(() => {
        this.setState({ dbPostNotice: "", clearStaged: false });
        this.props.notify(NotificationType.Clear);
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPostNotice = () => {
    this.setState({ postNotice: this.state.dbPostNotice });
    this.props.notify(NotificationType.Revert);
  };

  handlePostTextFieldChange = (
    e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
  ) => {
    this.setState({ postNotice: e.value });
  };

  componentDidMount = () => {
    getPostLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({
          dbPostNotice: response.data,
          postNotice: response.data
        });
      })
      .catch((error: AxiosError) => {
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
            <Button id="okToClear" onClick={this.handleClearPostNotice}>
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

  render() {
    const { postNotice, dbPostNotice } = this.state;
    const Dialogs = this.Dialogs;
    return (
      <SettingsMenuContainer>
        <Grid id="postLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField
              id="postNoticeField"
              variant="outlined"
              rows="12"
              rowsMax="35"
              multiline
              fullWidth
              inputProps={{ length: 12 }}
              placeholder={strings.postlogin.description}
              onChange={e => this.handlePostTextFieldChange(e.target)}
              value={postNotice}
            />
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button
                id="postClearButton"
                disabled={dbPostNotice == ""}
                onClick={this.stageClear}
                variant="text"
              >
                {commonString.action.clear}
              </Button>
            </Grid>
          </Grid>
        </Grid>
        <Dialogs />
      </SettingsMenuContainer>
    );
  }
}

export default PostLoginNoticeConfigurator;
