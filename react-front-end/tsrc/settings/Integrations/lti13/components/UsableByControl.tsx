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
import EditIcon from "@mui/icons-material/Edit";
import {
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as React from "react";
import { useEffect, useState } from "react";
import ACLExpressionBuilderDialog, {
  ACLExpressionBuilderDialogProps,
} from "../../../../components/ACLExpressionBuilderDialog";
import CodeBlock from "../../../../components/CodeBlock";
import SettingsListControl from "../../../../components/SettingsListControl";
import { TooltipIconButton } from "../../../../components/TooltipIconButton";
import {
  defaultACLEntityResolvers,
  generateHumanReadable,
  parse,
} from "../../../../modules/ACLExpressionModule";
import { languageStrings } from "../../../../util/langstrings";

const { edit: editLabel } = languageStrings.common.action;
const { usableBy, usableByDesc } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .accessControl;

export interface UsableByControlProps
  extends Pick<
    ACLExpressionBuilderDialogProps,
    | "searchUserProvider"
    | "searchGroupProvider"
    | "searchRoleProvider"
    | "aclEntityResolversProvider"
  > {
  /**
   * Initial value of AClExpression.
   */
  value: string;
  /**s
   * The handler when ACL Expression has been changed.
   */
  onChange: (result: string) => void;
}

const StyledListItemText = styled(ListItemText)(({ theme }) => ({
  "&": { marginRight: theme.spacing(5) },
}));

const editIconTitle = `${editLabel} ${usableBy}`;

/**
 * Used to build the ACL Expression to control who can use the platfrom.
 */
const UsableByControl = ({
  value,
  onChange,
  aclEntityResolversProvider = defaultACLEntityResolvers,
  searchUserProvider,
  searchGroupProvider,
  searchRoleProvider,
}: UsableByControlProps) => {
  const [showDialog, setShowDialog] = useState<boolean>(false);
  const [readableACLExpressionText, setReadableACLExpressionText] = useState(
    S.empty,
  );

  // generate human readable text for acl expression
  useEffect(() => {
    (async () => {
      const getName = pipe(
        value,
        parse,
        E.map(generateHumanReadable(aclEntityResolversProvider)),
        E.getOrElse(() => T.of(value)),
      );
      pipe(await getName(), setReadableACLExpressionText);
    })();
  }, [aclEntityResolversProvider, value]);

  return (
    <List>
      <SettingsListControl
        primaryText={usableBy}
        secondaryText={usableByDesc}
        control={undefined}
      />

      <ListItem>
        <StyledListItemText
          secondaryTypographyProps={{ component: "div" }}
          secondary={<CodeBlock value={readableACLExpressionText} />}
        />
        <ListItemSecondaryAction>
          <TooltipIconButton
            color="primary"
            title={editIconTitle}
            aria-label={editIconTitle}
            onClick={() => setShowDialog(true)}
          >
            <EditIcon fontSize="large"></EditIcon>
          </TooltipIconButton>
        </ListItemSecondaryAction>
      </ListItem>

      <ACLExpressionBuilderDialog
        aclEntityResolversProvider={aclEntityResolversProvider}
        value={value}
        onClose={(result) => {
          setShowDialog(false);
          result && onChange(result);
        }}
        open={showDialog}
        searchUserProvider={searchUserProvider}
        searchGroupProvider={searchGroupProvider}
        searchRoleProvider={searchRoleProvider}
      />
    </List>
  );
};

export default UsableByControl;
