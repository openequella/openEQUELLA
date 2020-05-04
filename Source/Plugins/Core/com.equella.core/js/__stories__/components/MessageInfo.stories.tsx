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
import { action } from "@storybook/addon-actions";
import { boolean, select, text } from "@storybook/addon-knobs";
import MessageInfo from "../../tsrc/components/MessageInfo";

export default {
  title: "MessageInfo",
  component: MessageInfo,
};

export const DynamicVariant = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant={select(
      "variant",
      { success: "success", error: "error", info: "info", warning: "warning" },
      "success"
    )}
  />
);

export const VariantSuccess = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="success"
  />
);

export const VariantError = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="error"
  />
);

export const VariantInfo = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="info"
  />
);

export const VariantWarning = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="warning"
  />
);
