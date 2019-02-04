import * as React from "react";
import {strings} from "./LoginNoticeConfigPage";
import {Button, Grid, TextField, Typography} from "@material-ui/core";
import {commonString} from "../util/commonstrings";
import {deletePostLoginNotice, getPostLoginNotice, submitPostLoginNotice} from "./LoginNoticeModule";
import {AxiosError, AxiosResponse} from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";

interface PostLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  onSaved: () => void;
  onDeleted: () => void;
  onUndone: () => void;
}

interface PostLoginNoticeConfiguratorState {
  postNotice?: string,               //what is currently in the textfield
  dbpostNotice?: string              //what is currently in the database
}

class PostLoginNoticeConfigurator extends React.Component<PostLoginNoticeConfiguratorProps, PostLoginNoticeConfiguratorState> {
  constructor(props: PostLoginNoticeConfiguratorProps) {
    super(props);
    this.state = ({
      postNotice: "",
      dbpostNotice: "",
    });
  };

  handleSubmitPostNotice = () => {
    if (this.state.postNotice != undefined) {
      submitPostLoginNotice(this.state.postNotice)
        .then(() => {
          this.props.onSaved();
          this.setState({dbpostNotice: this.state.postNotice});
        })
        .catch((error:AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleDeletePostNotice = () => {
    this.setState({postNotice: ""});
    deletePostLoginNotice()
      .then(() => {
        this.setState({dbpostNotice: ""});
        this.props.onDeleted();
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPostNotice = () => {
    this.setState({postNotice: this.state.dbpostNotice});
    this.props.onUndone();
  };

  handlePostTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({postNotice: e.value});
  };

  componentDidMount = () => {
    getPostLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({dbpostNotice: response.data, postNotice: response.data});
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  render() {
    const {postNotice, dbpostNotice} = this.state;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">{strings.postlogin.label}</Typography>
        <Grid id="postLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField id="postNoticeField"
                       rows="35"
                       variant="outlined"
                       multiline
                       fullWidth
                       placeholder={strings.postlogin.description}
                       onChange={e => this.handlePostTextFieldChange(e.target)}
                       value={postNotice}/>
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button id="postApplyButton"
                      disabled={postNotice == dbpostNotice}
                      onClick={this.handleSubmitPostNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="postDeleteButton"
                      disabled={dbpostNotice == ""}
                      onClick={this.handleDeletePostNotice}
                      variant="text">
                {commonString.action.delete}
              </Button>
            </Grid>
            <Grid item>
              <Button id="postUndoButton"
                      disabled={dbpostNotice == postNotice}
                      onClick={this.handleUndoPostNotice}
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

export default PostLoginNoticeConfigurator;
