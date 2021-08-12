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
import { Button, ButtonGroup } from "@material-ui/core";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

export interface SearchAttachmentsSelectorProps {
  /**
   * A boolean value indicating whether 'yes' or 'no' is selected.
   */
  value?: boolean;
  /**
   * Fired when a different option is selected.
   * @param value A boolean representing the selected option.
   */
  onChange: (value: boolean) => void;
}

export const SearchAttachmentsSelector = ({
  value,
  onChange,
}: SearchAttachmentsSelectorProps) => {
  const variant = (option?: boolean) => (option ? "contained" : "outlined");
  return (
    <ButtonGroup color="secondary">
      <Button variant={variant(value)} onClick={() => onChange(true)}>
        {languageStrings.common.action.yes}
      </Button>
      <Button variant={variant(!value)} onClick={() => onChange(false)}>
        {languageStrings.common.action.no}
      </Button>
    </ButtonGroup>
  );
};

export default SearchAttachmentsSelector;
