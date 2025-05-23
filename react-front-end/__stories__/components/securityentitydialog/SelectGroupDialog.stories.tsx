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
import { action } from "@storybook/addon-actions";
import { Meta } from "@storybook/react";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { eqGroupById } from "../../../tsrc/modules/GroupModule";
import SelectGroupDialog, {
  SelectGroupDialogProps,
} from "../../../tsrc/components/securityentitydialog/SelectGroupDialog";
import * as RS from "fp-ts/ReadonlySet";
import { searchGroups, groups } from "../../../__mocks__/GroupModule.mock";

export default {
  title: "component/SecurityEntityDialog/SelectGroupDialog",
  component: SelectGroupDialog,
} as Meta<SelectGroupDialogProps>;

const commonParams: SelectGroupDialogProps = {
  value: RS.empty,
  open: true,
  onClose: action("onClose"),
  searchGroupsProvider: searchGroups,
};

export const NoSelectedGroups = () => <SelectGroupDialog {...commonParams} />;

export const SelectedGroups = () => (
  <SelectGroupDialog
    {...commonParams}
    value={pipe(groups, RS.fromReadonlyArray(eqGroupById))}
  />
);

export const Skeleton = () => (
  <SelectGroupDialog {...commonParams} value={undefined} />
);
