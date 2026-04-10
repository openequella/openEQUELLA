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
import { Editor } from "@tinymce/tinymce-react";
import { AxiosPromise, AxiosResponse } from "axios";
import * as React from "react";
import "tinymce/tinymce";
import "tinymce/models/dom/model";
import "tinymce/themes/silver";
import "tinymce/icons/default";
import "tinymce/plugins/advlist";
import "tinymce/plugins/anchor";
import "tinymce/plugins/autolink";
import "tinymce/plugins/autoresize";
import "tinymce/plugins/charmap";
import "tinymce/plugins/code";
import "tinymce/plugins/codesample";
import "tinymce/plugins/directionality";
import "tinymce/plugins/emoticons";
import "tinymce/plugins/fullscreen";
import "tinymce/plugins/help";
import "tinymce/plugins/image";
import "tinymce/plugins/importcss";
import "tinymce/plugins/insertdatetime";
import "tinymce/plugins/link";
import "tinymce/plugins/lists";
import "tinymce/plugins/media";
import "tinymce/plugins/nonbreaking";
import "tinymce/plugins/pagebreak";
import "tinymce/plugins/preview";
import "tinymce/plugins/quickbars";
import "tinymce/plugins/save";
import "tinymce/plugins/searchreplace";
import "tinymce/plugins/table";
import "tinymce/plugins/visualblocks";
import "tinymce/plugins/visualchars";
import "tinymce/plugins/wordcount";
import { getBaseUrl, getRenderData } from "../AppConfig";
import "tinymce/plugins/emoticons/js/emojis";

const renderData = getRenderData();

// from https://github.com/tinymce/tinymce/blob/26b948ac85b75991ab9e50d0affdf4f5c0b34f65/modules/tinymce/src/core/main/ts/api/file/BlobCache.ts#L31-L39
export interface BlobInfo {
  id: () => string;
  name: () => string;
  filename: () => string;
  blob: () => Blob;
  base64: () => string;
  blobUri: () => string;
  uri: () => string | undefined;
}

export interface RichTextEditorProps {
  htmlInput?: string;
  /** Optionally provide the name of the skin to use. */
  skinName?: string;
  /** Optionally provide the URL for the location of the skin. */
  skinUrl?: string;
  onStateChange(html: string): void;
  imageUploadCallBack?(file: BlobInfo): AxiosPromise<ImageReturnType>;
}

interface ImageReturnType {
  link: string;
}

const RichTextEditor = ({
  htmlInput,
  skinName,
  skinUrl,
  onStateChange,
  imageUploadCallBack,
}: RichTextEditorProps) => {
  const uploadImages = (blobInfo: BlobInfo) =>
    new Promise<string>(
      (
        resolve: (value: PromiseLike<string> | string) => void,
        reject: (reason?: string) => void,
      ) => {
        if (imageUploadCallBack) {
          imageUploadCallBack(blobInfo)
            .then((response: AxiosResponse<ImageReturnType>) => {
              resolve(response.data.link);
            })
            .catch((error: Error) => {
              reject(error.name + error.message);
            });
        } else {
          reject("No upload path specified.");
        }
      },
    );

  const defaultSkinUrl =
    getBaseUrl() + renderData?.baseResources + "reactjs/tinymce/skins/ui/oxide";

  return (
    <Editor
      licenseKey="gpl"
      init={{
        min_height: 500,
        automatic_uploads: true,
        file_picker_types: "image",
        images_reuse_filename: true,
        images_upload_handler: uploadImages,
        paste_data_images: true,
        relative_urls: false,
        skin: skinName ?? "oxide",
        skin_url: skinUrl ?? defaultSkinUrl,
        media_dimensions: false,
      }}
      toolbar="formatselect | bold italic strikethrough underline forecolor backcolor | link image media file | alignleft aligncenter alignright alignjustify  | numlist bullist outdent indent hr | removeformat | undo redo | preview | ltr rtl"
      plugins={
        "advlist anchor autolink autoresize charmap code codesample " +
        "directionality emoticons fullscreen help image importcss" +
        "insertdatetime link lists media nonbreaking pagebreak preview " +
        "quickbars save searchreplace table visualblocks visualchars wordcount"
      }
      onEditorChange={onStateChange}
      value={htmlInput}
    />
  );
};

export default RichTextEditor;
