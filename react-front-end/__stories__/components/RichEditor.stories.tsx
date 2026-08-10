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
import type { Meta, StoryFn } from "@storybook/react";
import RichTextEditor, {
  RichTextEditorProps,
} from "../../tsrc/components/RichTextEditor";

export default {
  title: "RichTextEditor",
  component: RichTextEditor,
  argTypes: {
    onStateChange: { action: "onStateChange" },
    imageUploadCallBack: { action: "imageUploadCallBack" },
  },
} as Meta<RichTextEditorProps>;

export const WithoutHTMLInput: StoryFn<RichTextEditorProps> = (args) => (
  <RichTextEditor {...args} />
);
WithoutHTMLInput.args = {
  skinUrl: "http://localhost:6006/tinymce/skins/ui/oxide",
};

export const WithHTMLInput: StoryFn<RichTextEditorProps> = (args) => (
  <RichTextEditor {...args} />
);
WithHTMLInput.args = {
  ...WithoutHTMLInput.args,
  htmlInput: "<p>example</p>",
};
