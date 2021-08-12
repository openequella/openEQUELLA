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
import {
  createGenerateClassName,
  StylesProvider,
  ThemeProvider,
} from "@material-ui/core";
import * as React from "react";
import * as ReactDOM from "react-dom";
import { getRenderData } from "../AppConfig";
import {
  InlineFileUploader,
  InlineFileUploaderProps,
} from "./InlineFileUploader";
import {
  UniversalFileUploader,
  UniversalFileUploaderProps,
} from "./UniversalFileUploader";

// A type guard used to check if the props passed from server is 'InlineFileUploaderProps' or 'UniversalFileUploaderProps'.
const isInlineFileUploaderProps = (
  props: InlineFileUploaderProps | UniversalFileUploaderProps
): props is InlineFileUploaderProps =>
  (props as InlineFileUploaderProps).reloadState !== undefined;

/**
 * This function is created primarily for rendering either InlineFileUploader or UniversalFileUploader from server.
 * Four steps are required to do on server:
 * 1. Load "reactjs/scripts/uploadlist.js" by using 'IncludeFile'.
 * 2. Create a variable which references to the object 'FileUploader' provided by above JS file.
 * 3. Prepare required props by using 'ObjectExpression'.
 * 4. Pass the variable and props to any server function that can execute Javascript code(e.g. 'addReadyStatements').
 *
 * Please view 'UniversalWebControlNew.scala' and 'FileUploadHandlerNew' to get more details.
 *
 * @param props Props that will be passed to InlineFileUploader or UniversalFileUploader
 */
export const render = (
  props: InlineFileUploaderProps | UniversalFileUploaderProps
) => {
  const fileUploader = isInlineFileUploaderProps(props) ? (
    <InlineFileUploader {...props} />
  ) : (
    <UniversalFileUploader {...props} />
  );

  // In New UI, render the File Uploader with oEQ theme settings.
  if (getRenderData()?.newUI) {
    // ifu: InlineFileUploader, ufu: UniversalFileUploader
    const uniqueMUIStylePrefix = `oeq-${
      isInlineFileUploaderProps(props) ? "ifu" : "ufu"
    }`;
    const generateClassName = createGenerateClassName({
      productionPrefix: uniqueMUIStylePrefix,
      seed: uniqueMUIStylePrefix,
    });

    import("../theme/index")
      .then(({ oeqTheme }) => {
        ReactDOM.render(
          <StylesProvider generateClassName={generateClassName}>
            <ThemeProvider theme={oeqTheme}>{fileUploader}</ThemeProvider>
          </StylesProvider>,
          props.elem
        );
      })
      .catch((error: Error) =>
        console.error(`Fail to load oEQ Theme settings due to ${error.message}`)
      );
  } else {
    ReactDOM.render(fileUploader, props.elem);
  }
};
