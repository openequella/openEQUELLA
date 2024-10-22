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
import { PushPin, PushPinOutlined } from "@mui/icons-material";
import * as React from "react";
import { useState } from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import SearchResult, {
  defaultActionButtonProps,
} from "../../search/components/SearchResult";
import { languageStrings } from "../../util/langstrings";
import HierarchyKeyResourceDialog from "./HierarchyKeyResourceDialog";
import * as OEQ from "@openequella/rest-api-client";

const {
  addKeyResource: addKeyResourceText,
  removeKeyResource: removeKeyResourceText,
} = languageStrings.hierarchy;

interface HierarchySearchResultProps {
  /** The search result item. */
  item: OEQ.Search.SearchResultItem;
  /** A list of highlight terms. */
  highlights: string[];
  /** `true` if the user has the permission to modify key resource ACLs, and show the pin icon. */
  hasModifyKeyResourceAcl: boolean;
  /**
   * `true` if the item is a key resource.
   */
  isKeyResource: boolean;
  /** Function to add/remove a key resource. */
  updateKeyResource: (
    itemUuid: OEQ.Common.UuidString,
    itemVersion: number,
    isDelete: boolean,
    isAlwaysLatest?: boolean,
  ) => Promise<void>;
}

/**
 * Search result item with update key resource button and a dialog for user to select item version.
 */
const HierarchySearchResult = ({
  item,
  highlights,
  isKeyResource,
  hasModifyKeyResourceAcl,
  updateKeyResource,
}: HierarchySearchResultProps): React.ReactNode => {
  const [showAddKeyResourceDialog, setShowAddKeyResourceDialog] =
    useState(false);

  const { uuid, version, isLatestVersion } = item;
  const title = isKeyResource ? removeKeyResourceText : addKeyResourceText;
  const updateKeyResourceButton = (
    <TooltipIconButton
      title={title}
      onClick={() => setShowAddKeyResourceDialog(true)}
      size="small"
    >
      {isKeyResource ? <PushPin color="secondary" /> : <PushPinOutlined />}
    </TooltipIconButton>
  );

  return (
    <>
      <SearchResult
        item={item}
        highlights={highlights}
        actionButtonConfig={{
          ...defaultActionButtonProps,
          showAddToHierarchy: false,
        }}
        customActionButtons={
          hasModifyKeyResourceAcl ? [updateKeyResourceButton] : undefined
        }
      />
      <HierarchyKeyResourceDialog
        itemUuid={uuid}
        itemVersion={version}
        isLatestVersion={isLatestVersion}
        isKeyResource={isKeyResource}
        updateKeyResource={updateKeyResource}
        open={showAddKeyResourceDialog}
        closeDialog={() => setShowAddKeyResourceDialog(false)}
      />
    </>
  );
};

export default HierarchySearchResult;
