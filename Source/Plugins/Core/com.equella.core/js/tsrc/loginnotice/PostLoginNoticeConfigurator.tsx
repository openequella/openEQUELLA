import * as React from "react";
import {strings} from "./LoginNoticeConfigPage";
import {Button, DialogContent, DialogContentText, Grid, TextField, Typography} from "@material-ui/core";
import {commonString} from "../util/commonstrings";
import {deletePostLoginNotice, getPostLoginNotice, submitPostLoginNotice} from "./LoginNoticeModule";
import {AxiosError, AxiosResponse} from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";

interface PostLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  onSaved: () => void;
  onDeleted: () => void;
  onUndone: () => void;
}

interface PostLoginNoticeConfiguratorState {
  postNotice?: string,               //what is currently in the textfield
  dbPostNotice?: string              //what is currently in the database
  deleteStaged: boolean
}

class PostLoginNoticeConfigurator extends React.Component<PostLoginNoticeConfiguratorProps, PostLoginNoticeConfiguratorState> {
  constructor(props: PostLoginNoticeConfiguratorProps) {
    super(props);
    this.state = ({
      postNotice: "",
      dbPostNotice: "",
      deleteStaged: false
    });
  };

  handleSubmitPostNotice = () => {
    if (this.state.postNotice != undefined) {
      submitPostLoginNotice(this.state.postNotice)
        .then(() => {
          this.props.onSaved();
          this.setState({dbPostNotice: this.state.postNotice});
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
        this.setState({dbPostNotice: "", deleteStaged:false});
        this.props.onDeleted();
      })
      .catch((error:AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPostNotice = () => {
    this.setState({postNotice: this.state.dbPostNotice});
    this.props.onUndone();
  };

  handlePostTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({postNotice: e.value});
  };

  componentDidMount = () => {
    getPostLoginNotice()
      .then((response: AxiosResponse) => {
        this.setState({dbPostNotice: response.data, postNotice: response.data});
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
            <Button onClick={this.handleDeletePostNotice}>{commonString.action.ok}</Button>
            <Button onClick={() =>this.setState({deleteStaged:false})}>{commonString.action.cancel}</Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  };

  render() {
    const {postNotice, dbPostNotice} = this.state;
    const Dialogs = this.Dialogs;
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
                      disabled={postNotice == dbPostNotice}
                      onClick={this.handleSubmitPostNotice}
                      variant="contained">
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button id="postDeleteButton"
                      disabled={dbPostNotice == ""}
                      onClick={this.stageDelete}
                      variant="text">
                {commonString.action.delete}
              </Button>
            </Grid>
            <Grid item>
              <Button id="postUndoButton"
                      disabled={dbPostNotice == postNotice}
                      onClick={this.handleUndoPostNotice}
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

export default PostLoginNoticeConfigurator;
