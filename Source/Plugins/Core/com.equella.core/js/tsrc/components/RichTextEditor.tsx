import * as React from "react";
import { Editor } from "@tinymce/tinymce-react";
import { AxiosPromise, AxiosResponse } from "axios";
import { Config } from "../config";

require("tinymce/tinymce");
require("tinymce/themes/silver/theme");
require("tinymce/plugins/anchor");
require("tinymce/plugins/advlist");
require("tinymce/plugins/autolink");
require("tinymce/plugins/autoresize");
require("tinymce/plugins/charmap");
require("tinymce/plugins/code");
require("tinymce/plugins/codesample");
require("tinymce/plugins/directionality");
require("tinymce/plugins/fullscreen");
require("tinymce/plugins/help");
require("tinymce/plugins/hr");
require("tinymce/plugins/image");
require("tinymce/plugins/imagetools");
require("tinymce/plugins/importcss");
require("tinymce/plugins/insertdatetime");
require("tinymce/plugins/link");
require("tinymce/plugins/lists");
require("tinymce/plugins/media");
require("tinymce/plugins/nonbreaking");
require("tinymce/plugins/noneditable");
require("tinymce/plugins/pagebreak");
require("tinymce/plugins/paste");
require("tinymce/plugins/preview");
require("tinymce/plugins/print");
require("tinymce/plugins/quickbars");
require("tinymce/plugins/save");
require("tinymce/plugins/searchreplace");
require("tinymce/plugins/table");
require("tinymce/plugins/template");
require("tinymce/plugins/textpattern");
require("tinymce/plugins/toc");
require("tinymce/plugins/visualblocks");
require("tinymce/plugins/visualchars");
require("tinymce/plugins/wordcount");

declare const renderData:
  | {
      baseResources: string;
      newUI: boolean;
      autotestMode: boolean;
    }
  | undefined;

// from https://github.com/tinymce/tinymce/blob/26b948ac85b75991ab9e50d0affdf4f5c0b34f65/modules/tinymce/src/core/main/ts/api/file/BlobCache.ts#L31-L39
export interface BlobInfo {
  id: () => string;
  name: () => string;
  filename: () => string;
  blob: () => Blob;
  base64: () => string;
  blobUri: () => string;
  uri: () => string;
}

interface RichTextEditorProps {
  htmlInput?: string;
  onStateChange(html: string): void;
  imageUploadCallBack?(file: BlobInfo): AxiosPromise<ImageReturnType>;
}

interface ImageReturnType {
  link: string;
}

interface RichTextEditorState {
  ready: boolean;
}

class RichTextEditor extends React.Component<
  RichTextEditorProps,
  RichTextEditorState
> {
  constructor(props: RichTextEditorProps) {
    super(props);
    this.state = { ready: false };
  }

  componentDidMount = () => {
    setTimeout(() => {
      // this is a workaround for something related to: https://github.com/tinymce/tinymce-angular/issues/76
      this.setState({ ready: true });
    }, 1);
  };

  uploadImages = (
    blobInfo: BlobInfo,
    success: (msg: string) => void,
    failure: (msg: string) => void
  ): void => {
    if (this.props.imageUploadCallBack) {
      this.props
        .imageUploadCallBack(blobInfo)
        .then((response: AxiosResponse<ImageReturnType>) =>
          success(response.data.link)
        )
        .catch((error: Error) => failure(error.name + error.message));
    } else {
      failure("No upload path specified.");
    }
  };

  render() {
    const skinUrl =
      Config.baseUrl +
      renderData?.baseResources +
      "reactjs/tinymce/skins/ui/oxide";

    return (
      this.state.ready && (
        <Editor
          init={{
            min_height: 500,
            automatic_uploads: true,
            file_picker_types: "image",
            images_reuse_filename: true,
            images_upload_handler: this.uploadImages,
            paste_data_images: true,
            relative_urls: false,
            skin: "oxide",
            skin_url: skinUrl,
            media_dimensions: false,
          }}
          toolbar={
            "formatselect | bold italic strikethrough underline forecolor backcolor | link image media file | alignleft aligncenter alignright alignjustify  | numlist bullist outdent indent hr | removeformat | undo redo | preview | ltr rtl"
          }
          plugins={
            "anchor autolink autoresize advlist charmap code codesample  " +
            "directionality fullscreen help hr image imagetools " +
            "importcss insertdatetime link lists media nonbreaking noneditable pagebreak paste " +
            "preview print quickbars save searchreplace table template " +
            "textpattern toc visualblocks visualchars wordcount"
          }
          onEditorChange={this.props.onStateChange}
          value={this.props.htmlInput}
        />
      )
    );
  }
}

export default RichTextEditor;
