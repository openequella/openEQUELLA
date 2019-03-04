import * as React from "react";
import {
  Button,
  DialogContent,
  DialogContentText,
  Grid,
  Typography
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  strings,
  submitPreLoginNotice
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";
import { Editor } from "react-draft-wysiwyg";
import {
  ContentState,
  convertFromRaw,
  convertToRaw,
  EditorState,
  RawDraftContentState
} from "draft-js";

let draftjsToHtml: Function = require("draftjs-to-html");
let htmlToDraft: Function = require("html-to-draftjs").default;

// require("react-draft-wysiwyg/dist/react-draft-wysiwyg.css");
interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PreLoginNoticeConfiguratorState {
  editorState: EditorState;
  dbContentState: RawDraftContentState;
  clearStaged: boolean;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      editorState: EditorState.createEmpty(),
      dbContentState: convertToRaw(ContentState.createFromText("")),
      clearStaged: false
    };
  }

  fixImageAlignment = (entity: any) => {
    //has to be any, as entity has a different type depending on its tag
    if (entity.type != null) {
      if (entity.type == "IMAGE") {
        let textAlign = "center";
        if (entity.data.alignment) {
          //entity.data.alignment is for float using the LCR options on the image
          //'none' means the user clicked center
          switch (entity.data.alignment) {
            case "none":
              textAlign = "center";
              break;
            case "left":
              textAlign = "left";
              break;
            case "right":
              textAlign = "right";
              break;
          }
        }
        return (
          '<imagealign style="display:flex; justify-content:' +
          textAlign +
          ';">' +
          '<img src="' +
          entity.data.src +
          '"style="height: ' +
          entity.data.height +
          ";width: " +
          entity.data.width +
          ";float: " +
          (entity.data.alignment || "none") +
          '"/>' +
          "</imagealign>"
        );
      }
    }
    return null;
  };

  handleSubmitPreNotice = () => {
    let raw = draftjsToHtml(
      convertToRaw(this.state.editorState.getCurrentContent()),
      {
        trigger: "#",
        separator: " "
      },
      false,
      (entity: any) => this.fixImageAlignment(entity)
    );
    submitPreLoginNotice(raw)
      .then(() => {
        this.props.notify(NotificationType.Save);
        this.setState({
          dbContentState: convertToRaw(
            this.state.editorState.getCurrentContent()
          )
        });
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleClearPreNotice = () => {
    clearPreLoginNotice()
      .then(() => {
        this.setState({
          editorState: EditorState.createEmpty(),
          dbContentState: convertToRaw(ContentState.createFromText("")),
          clearStaged: false
        });
        this.props.notify(NotificationType.Clear);
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPreNotice = () => {
    this.setState({
      editorState: EditorState.createWithContent(
        convertFromRaw(this.state.dbContentState)
      )
    });
    this.props.notify(NotificationType.Revert);
  };

  stripAlignmentStyles = (nodeName: string, node: HTMLElement) => {
    if (nodeName === "imagealign") {
      return node.getElementsByClassName("img").item(0);
    }
    return null;
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        const blocksFromHtml = htmlToDraft(
          response.data,
          (nodeName: string, node: HTMLElement) =>
            this.stripAlignmentStyles(nodeName, node)
        );
        const { contentBlocks, entityMap } = blocksFromHtml;
        const contentState = ContentState.createFromBlockArray(
          contentBlocks,
          entityMap
        );
        const editorState = EditorState.createWithContent(contentState);
        this.setState({
          dbContentState: convertToRaw(contentState),
          editorState: editorState
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

  render() {
    const Dialogs = this.Dialogs;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">
          {strings.prelogin.label}
        </Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <Editor
              editorState={this.state.editorState}
              onEditorStateChange={(editorState: EditorState) =>
                this.setState({ editorState })
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
              >
                {commonString.action.clear}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
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
