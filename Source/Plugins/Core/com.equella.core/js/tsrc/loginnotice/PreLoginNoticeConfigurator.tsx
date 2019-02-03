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
}

interface PreLoginNoticeConfiguratorState {
  preNotice?: string
}

class PreLoginNoticeConfigurator extends React.Component<PreLoginNoticeConfiguratorProps, PreLoginNoticeConfiguratorState> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
  };

  state: PreLoginNoticeConfiguratorState = {
    preNotice: ""
  };

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      submitPreLoginNotice(this.state.preNotice)
        .then(this.props.onSaved)
        .catch((error) => {
          this.props.handleError(error);
        });
    }
  };

  handleDeletePreNotice = () => {
    this.setState({preNotice: ""});
    deletePreLoginNotice()
      .then(this.props.onDeleted)
      .catch((error) => {
        this.props.handleError(error);
      });
  };

  handlePreTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({preNotice: e.value});
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({preNotice: response.data});
      })
      .catch((error) => {
        this.props.handleError(error);
      });
  };

  render() {
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
                       value={this.state.preNotice}/>
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button id="preApplyButton"
                      onClick={this.handleSubmitPreNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="preDeleteButton"
                      disabled={this.state.preNotice == ""}
                      onClick={this.handleDeletePreNotice}
                      variant="text">
                {commonString.action.delete}
              </Button>
            </Grid>
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    )
  }
}

export default PreLoginNoticeConfigurator;
