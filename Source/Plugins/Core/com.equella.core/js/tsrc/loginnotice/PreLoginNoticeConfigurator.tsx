import * as React from "react";
import {strings} from "./LoginNoticeConfigPage";
import {Button, DialogContent, DialogContentText, Grid, TextField, Typography} from "@material-ui/core";
import {commonString} from "../util/commonstrings";
import {deletePreLoginNotice, getPreLoginNotice, submitPreLoginNotice} from "./LoginNoticeModule";
import {AxiosError, AxiosResponse} from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  onSaved: () => void;
  onDeleted: () => void;
  onUndone: () => void;
}

interface PreLoginNoticeConfiguratorState {
  preNotice?: string,               //what is currently in the textfield
  dbPreNotice?: string              //what is currently in the database
  deleteStaged: boolean
}

class PreLoginNoticeConfigurator extends React.Component<PreLoginNoticeConfiguratorProps, PreLoginNoticeConfiguratorState> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = ({
      preNotice: "",
      dbPreNotice: "",
      deleteStaged: false
    });
  };

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      submitPreLoginNotice(this.state.preNotice)
        .then(() => {
          this.props.onSaved();
          this.setState({dbPreNotice: this.state.preNotice});
        })
        .catch((error:AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleDeletePreNotice = () => {
    this.setState({preNotice: ""});
    deletePreLoginNotice()
      .then(() => {
        this.setState({dbPreNotice: "", deleteStaged:false});
        this.props.onDeleted();
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPreNotice = () => {
    this.setState({preNotice: this.state.dbPreNotice});
    this.props.onUndone();
  };

  handlePreTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({preNotice: e.value});
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({dbPreNotice: response.data, preNotice: response.data});
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  stageDelete = () => {
    this.setState({deleteStaged:true});
  };

  Dialogs = () => {
    return(
      <div>
        <Dialog open={this.state.deleteStaged} onClose={()=>this.setState({deleteStaged:false})}>
          <DialogTitle>{strings.delete.title}</DialogTitle>
          <DialogContent>
            <DialogContentText>
              {strings.delete.confirm}
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleDeletePreNotice}>{commonString.action.ok}</Button>
            <Button onClick={() =>this.setState({deleteStaged:false})}>{commonString.action.cancel}</Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  };

  render() {
    const {preNotice, dbPreNotice} = this.state;
    const Dialogs = this.Dialogs;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">{strings.prelogin.label}</Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField id="preNoticeField"
                       variant="outlined"
                       rows="35"
                       multiline
                       fullWidth
                       inputProps={{ length: 12 }}
                       placeholder={strings.prelogin.description}
                       onChange={e => this.handlePreTextFieldChange(e.target)}
                       value={preNotice}/>
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button id="preApplyButton"
                      disabled={preNotice == dbPreNotice}
                      onClick={this.handleSubmitPreNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="preDeleteButton"
                      disabled={dbPreNotice == ""}
                      onClick={this.stageDelete}
                      variant="text">
                {commonString.action.delete}
              </Button>
            </Grid>
            <Grid item>
              <Button id="preUndoButton"
                      disabled={dbPreNotice == preNotice}
                      onClick={this.handleUndoPreNotice}
                      variant="text">
                {commonString.action.revertchanges}
              </Button>
            </Grid>
          </Grid>
        </Grid>
        <Dialogs/>
      </SettingsMenuContainer>
    )
  }
}

export default PreLoginNoticeConfigurator;
