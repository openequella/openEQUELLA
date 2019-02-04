import * as React from "react";
import {strings} from "./LoginNoticeConfigPage";
import {Button, Grid, TextField, Typography} from "@material-ui/core";
import {commonString} from "../util/commonstrings";
import {deletePreLoginNotice, getPreLoginNotice, submitPreLoginNotice} from "./LoginNoticeModule";
import {AxiosError, AxiosResponse} from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  onSaved: () => void;
  onDeleted: () => void;
  onUndone: () => void;
}

interface PreLoginNoticeConfiguratorState {
  preNotice?: string,               //what is currently in the textfield
  dbpreNotice?: string              //what is currently in the database
}

class PreLoginNoticeConfigurator extends React.Component<PreLoginNoticeConfiguratorProps, PreLoginNoticeConfiguratorState> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = ({
      preNotice: "",
      dbpreNotice: "",
    });
  };

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      submitPreLoginNotice(this.state.preNotice)
        .then(() => {
          this.props.onSaved();
          this.setState({dbpreNotice: this.state.preNotice});
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
        this.setState({dbpreNotice: ""});
        this.props.onDeleted();
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPreNotice = () => {
    this.setState({preNotice: this.state.dbpreNotice});
    this.props.onUndone();
  };

  handlePreTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({preNotice: e.value});
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({dbpreNotice: response.data, preNotice: response.data});
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  render() {
    const {preNotice, dbpreNotice} = this.state;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">{strings.prelogin.label}</Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField id="preNoticeField"
                       rows="35"
                       variant="outlined"
                       multiline
                       fullWidth
                       placeholder={strings.prelogin.description}
                       onChange={e => this.handlePreTextFieldChange(e.target)}
                       value={preNotice}/>
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button id="preApplyButton"
                      disabled={preNotice == dbpreNotice}
                      onClick={this.handleSubmitPreNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="preDeleteButton"
                      disabled={dbpreNotice == ""}
                      onClick={this.handleDeletePreNotice}
                      variant="text">
                {commonString.action.delete}
              </Button>
            </Grid>
            <Grid item>
              <Button id="preUndoButton"
                      disabled={dbpreNotice == preNotice}
                      onClick={this.handleUndoPreNotice}
                      variant="text">
                {commonString.action.revertchanges}
              </Button>
            </Grid>
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    )
  }
}

export default PreLoginNoticeConfigurator;
