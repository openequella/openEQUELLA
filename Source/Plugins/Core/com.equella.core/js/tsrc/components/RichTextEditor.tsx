import { Editor } from "react-draft-wysiwyg";
import { ContentState, convertToRaw, EditorState } from "draft-js";
import * as React from "react";

let draftjsToHtml: Function = require("draftjs-to-html");
let htmlToDraft: Function = require("html-to-draftjs").default;

require("react-draft-wysiwyg/dist/react-draft-wysiwyg.css");

interface RichTextEditorProps {
  htmlInput: string;
  onStateChange(html: string): void;
}

interface RichTextEditorState {
  editorState: EditorState;
}

class RichTextEditor extends React.Component<
  RichTextEditorProps,
  RichTextEditorState
> {
  constructor(props: RichTextEditorProps) {
    super(props);
    this.state = {
      editorState: EditorState.createEmpty()
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
        return `<imagealign style="display: flex; justify-content: ${textAlign};">
          <img src= "${entity.data.src}" alt="" style="height: ${
          entity.data.height
        }; width: ${entity.data.width}; float: ${
          entity.data.alignment == undefined ? "none" : entity.data.alignment
        };"/>
          </imagealign>`;
      }
    }
    return null;
  };

  toHTML = (editorState: EditorState): string => {
    let html: string = draftjsToHtml(
      convertToRaw(editorState.getCurrentContent()),
      {
        trigger: "#",
        separator: " "
      },
      false,
      (entity: any) => this.fixImageAlignment(entity)
    ).trim();
    return html == "<p></p>" ? "" : html;
  };

  toDraft = (html: string) => {
    const blocksFromHtml = htmlToDraft(
      html,
      (nodeName: string, node: HTMLElement) =>
        this.stripAlignmentStyles(nodeName, node)
    );
    const { contentBlocks, entityMap } = blocksFromHtml;
    const contentState = ContentState.createFromBlockArray(
      contentBlocks,
      entityMap
    );
    this.setState({ editorState: EditorState.createWithContent(contentState) });
  };

  handleEditorChange = (editorState: EditorState) => {
    this.setState({ editorState });
    this.props.onStateChange(this.toHTML(this.state.editorState));
  };

  componentWillReceiveProps(
    nextProps: Readonly<RichTextEditorProps>,
    nextContext: any
  ): void {
    if (nextProps.htmlInput !== this.props.htmlInput) {
      this.toDraft(nextProps.htmlInput);
    }
  }

  stripAlignmentStyles = (nodeName: string, node: HTMLElement) => {
    if (nodeName === "imagealign") {
      return node.getElementsByClassName("img").item(0);
    }
    return null;
  };

  render() {
    return (
      <Editor
        editorState={this.state.editorState}
        onEditorStateChange={this.handleEditorChange}
      />
    );
  }
}

export default RichTextEditor;
