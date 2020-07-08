/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from "react";
import { Editor } from "@tinymce/tinymce-react";
import { AxiosPromise, AxiosResponse } from "axios";
import { Config } from "../config";

import "tinymce/tinymce";

import "tinymce/icons/default";
import "tinymce/themes/silver/theme";

import "tinymce/plugins/anchor";
import "tinymce/plugins/advlist";
import "tinymce/plugins/autolink";
import "tinymce/plugins/autoresize";
import "tinymce/plugins/charmap";
import "tinymce/plugins/code";
import "tinymce/plugins/codesample";
import "tinymce/plugins/directionality";
import "tinymce/plugins/fullscreen";
import "tinymce/plugins/help";
import "tinymce/plugins/hr";
import "tinymce/plugins/image";
import "tinymce/plugins/imagetools";
import "tinymce/plugins/importcss";
import "tinymce/plugins/insertdatetime";
import "tinymce/plugins/link";
import "tinymce/plugins/lists";
import "tinymce/plugins/media";
import "tinymce/plugins/nonbreaking";
import "tinymce/plugins/noneditable";
import "tinymce/plugins/pagebreak";
import "tinymce/plugins/paste";
import "tinymce/plugins/preview";
import "tinymce/plugins/print";
import "tinymce/plugins/quickbars";
import "tinymce/plugins/save";
import "tinymce/plugins/searchreplace";
import "tinymce/plugins/table";
import "tinymce/plugins/template";
import "tinymce/plugins/textpattern";
import "tinymce/plugins/toc";
import "tinymce/plugins/visualblocks";
import "tinymce/plugins/visualchars";
import "tinymce/plugins/wordcount";

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
          toolbar="formatselect | bold italic strikethrough underline forecolor backcolor | link image media file | alignleft aligncenter alignright alignjustify  | numlist bullist outdent indent hr | removeformat | undo redo | preview | ltr rtl"
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
