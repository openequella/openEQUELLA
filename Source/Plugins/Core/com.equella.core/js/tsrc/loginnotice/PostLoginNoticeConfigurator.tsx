import * as React from "react";
import {strings} from "./LoginNoticeConfigPage";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import {Button, Grid, TextField} from "@material-ui/core";
import {commonString} from "../util/commonstrings";
import {deletePostLoginNotice, getPostLoginNotice, submitPostLoginNotice} from "./LoginNoticeModule";
import {AxiosError, AxiosResponse} from "axios";
import Typography from "@material-ui/core/Typography";

interface PostLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  onSaved: () => void;
  onDeleted: () => void;
}

interface PostLoginNoticeConfiguratorState {
  postNotice?: string
}

class PostLoginNoticeConfigurator extends React.Component<PostLoginNoticeConfiguratorProps, PostLoginNoticeConfiguratorState> {
  constructor(props: PostLoginNoticeConfiguratorProps) {
    super(props);
  };

  state: PostLoginNoticeConfiguratorState = {
    postNotice: ""
  };

  handleSubmitPostNotice = () => {
    if (this.state.postNotice != undefined) {
      submitPostLoginNotice(this.state.postNotice)
        .then(this.props.onSaved)
        .catch((error) => {
          this.props.handleError(error);
        });
    }
  };

  handleDeletePostNotice = () => {
    this.setState({postNotice: ""});
    deletePostLoginNotice()
      .then(this.props.onDeleted)
      .catch((error) => {
        this.props.handleError(error);
      });
  };

  handlePostTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({postNotice: e.value});
  };

  componentDidMount = () => {
    getPostLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({postNotice: response.data});
      })
      .catch((error) => {
        this.props.handleError(error);
      });
  };

  render() {
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">{strings.postlogin.label}</Typography>
        <Grid id="postLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField id="postNoticeField"
                       rows="35"
                       fullWidth
                       variant="outlined"
                       multiline
                       placeholder={strings.postlogin.description}
                       onChange={e => this.handlePostTextFieldChange(e.target)}
                       value={this.state.postNotice}/>
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button id="postApplyButton"
                      onClick={this.handleSubmitPostNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="postDeleteButton"
                      disabled={this.state.postNotice == ""}
                      onClick={this.handleDeletePostNotice}
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

export default PostLoginNoticeConfigurator;
