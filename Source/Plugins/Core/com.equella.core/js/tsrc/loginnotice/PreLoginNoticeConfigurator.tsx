import * as React from "react";
import {
  Button,
  DialogContent,
  DialogContentText,
  Grid,
  TextField,
  Typography
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  submitPreLoginNotice,
  strings
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PreLoginNoticeConfiguratorState {
  preNotice?: string; //what is currently in the textfield
  dbPreNotice?: string; //what is currently in the database
  clearStaged: boolean;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      preNotice: "",
      dbPreNotice: "",
      clearStaged: false
    };
  }

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      submitPreLoginNotice(this.state.preNotice)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setState({ dbPreNotice: this.state.preNotice });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleClearPreNotice = () => {
    this.setState({ preNotice: "" });
    clearPreLoginNotice()
      .then(() => {
        this.setState({ dbPreNotice: "", clearStaged: false });
        this.props.notify(NotificationType.Clear);
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPreNotice = () => {
    this.setState({ preNotice: this.state.dbPreNotice });
    this.props.notify(NotificationType.Revert);
  };

  handlePreTextFieldChange = (
    e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
  ) => {
    this.setState({ preNotice: e.value });
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({ dbPreNotice: response.data, preNotice: response.data });
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

  render() {
    const { preNotice, dbPreNotice } = this.state;
    const Dialogs = this.Dialogs;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">
          {strings.prelogin.label}
        </Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField
              id="preNoticeField"
              variant="outlined"
              rows="12"
              rowsMax="35"
              multiline
              fullWidth
              inputProps={{ length: 12 }}
              placeholder={strings.prelogin.description}
              onChange={e => this.handlePreTextFieldChange(e.target)}
              value={preNotice}
            />
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button
                id="preApplyButton"
                disabled={preNotice == dbPreNotice}
                onClick={this.handleSubmitPreNotice}
                variant="contained"
              >
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preClearButton"
                disabled={dbPreNotice == ""}
                onClick={this.stageClear}
                variant="text"
              >
                {commonString.action.clear}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
                disabled={dbPreNotice == preNotice}
                onClick={this.handleUndoPreNotice}
                variant="text"
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
