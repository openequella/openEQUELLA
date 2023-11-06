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
import ErrorOutline from "@mui/icons-material/ErrorOutline";
import {
  Button,
  Divider,
  FormControl,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  MenuItem,
  Select,
  Typography,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { flow, pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as M from "fp-ts/Map";
import * as S from "fp-ts/string";
import * as O from "fp-ts/Option";
import * as SET from "fp-ts/Set";
import * as React from "react";
import ConfirmDialog from "../../../components/ConfirmDialog";
import RoleSearch from "../../../components/securityentitysearch/RoleSearch";
import SecurityEntityEntry from "../../../components/securityentitydialog/SecurityEntityEntry";
import {
  defaultSelectedRoleUrn,
  getRoleNameByUrn,
  ltiRoles,
} from "../../../modules/Lti13PlatformsModule";
import { eqRoleById, ordRole } from "../../../modules/RoleModule";
import { languageStrings } from "../../../util/langstrings";

const {
  customRoleDialogTitle,
  customRoleTableLtiRoleColumn,
  customRoleTableOeqRoleColumn,
  customRoleCurrentSelections,
  customRoleSelectLtiRole,
  customRoleSelectOeqRole,
  customRoleAddRoles,
} =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;
const { ok: okLabel, removeAll: removeAllLabel } =
  languageStrings.common.action;

const StyledDivider = styled(Divider)(({ theme }) => ({
  marginLeft: theme.spacing(2),
  marginRight: theme.spacing(2),
}));

const StyledTypography = styled(Typography)(({ theme }) => ({
  marginTop: theme.spacing(1),
}));

export interface SelectCustomRoleDialogProps {
  /** Open the dialog when true. */
  open: boolean;
  /** The currently roles mapping. */
  value: Map<string, Set<OEQ.UserQuery.RoleDetails>>;
  /** Handler for when dialog is closed. */
  onClose: (selections?: Map<string, Set<OEQ.UserQuery.RoleDetails>>) => void;
  /** Function which will provide the list of Role (search function) for RoleSearch. */
  roleListProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * This dialog used for selecting LTI role and searching for oEQ roles,
 * and making selections of oEQ roles. The search component and LTI role selector is displayed on the left hand side,
 * from which a user can make selections which will then be added to a table on the right hand side.
 * When the user confirms the dialog (click's OK),
 * that roles mapping on the right hand side is returned to the calling component.
 */
const SelectCustomRoleDialog = ({
  open,
  value,
  onClose,
  roleListProvider,
}: SelectCustomRoleDialogProps) => {
  const [selectedLtiRole, setSelectedLtiRole] = React.useState(
    defaultSelectedRoleUrn,
  );
  // state for final selections which will be returned to the calling component when the user clicks OK
  const [rolesMapping, setRolesMapping] =
    React.useState<Map<string, Set<OEQ.UserQuery.RoleDetails>>>(value);

  const LtiRoleSelect = () => (
    <FormControl fullWidth>
      <Select
        value={selectedLtiRole}
        onChange={(event) => setSelectedLtiRole(event.target.value)}
        MenuProps={{
          PaperProps: {
            style: {
              maxHeight: 280,
            },
          },
        }}
        aria-label={customRoleSelectLtiRole}
      >
        {ltiRoles.map(({ name, urn }) => (
          <MenuItem key={urn} value={urn}>
            <ListItemText primary={name} secondary={urn} />
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );

  // oEQ role entry in the `oEQ column`
  const oeqRoleEntry = (
    ltiRoleUri: string,
    role: OEQ.UserQuery.RoleDetails,
  ) => (
    <SecurityEntityEntry
      key={role.id}
      name={role.name}
      onDelete={() =>
        pipe(
          rolesMapping,
          // get current set of oEQ roles for corresponding lti role
          M.lookup(S.Eq)(ltiRoleUri),
          // remove selected role
          O.map(flow(SET.remove(eqRoleById)(role))),
          // replace old oeq role set in map
          O.chain((newRoles) =>
            SET.isEmpty(newRoles)
              ? pipe(rolesMapping, M.deleteAt(S.Eq)(ltiRoleUri), O.of)
              : pipe(rolesMapping, M.updateAt(S.Eq)(ltiRoleUri, newRoles)),
          ),
          O.map(setRolesMapping),
          O.getOrElse(() =>
            console.warn(`Can't find lti role: ${ltiRoleUri} in map`),
          ),
        )
      }
    />
  );

  // the row of each lti role
  const ltiRoleRow = (
    ltiRoleUri: string,
    ltiRoleName: string,
    roles: Set<OEQ.UserQuery.RoleDetails>,
  ) => (
    <TableRow
      key={ltiRoleUri}
      sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
    >
      <TableCell component="th" scope="row">
        {ltiRoleName}
      </TableCell>
      <TableCell align="right">
        <List>
          {pipe(
            roles,
            SET.toArray(ordRole),
            A.map((r) => oeqRoleEntry(ltiRoleUri, r)),
          )}
        </List>
      </TableCell>
    </TableRow>
  );

  // the table shows the mapping relationships between lti roles and oEQ roles
  const ltiOeqRoleMappingTable = (
    roleMaps: Map<string, Set<OEQ.UserQuery.RoleDetails>>,
  ) => (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>{customRoleTableLtiRoleColumn}</TableCell>
            <TableCell>{customRoleTableOeqRoleColumn}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {pipe(
            roleMaps,
            M.reduceWithIndex(S.Ord)<
              JSX.Element[],
              Set<OEQ.UserQuery.RoleDetails>
            >([], (ltiRoleUri, result, oEQRoles) => {
              const ltiRoleName = getRoleNameByUrn(ltiRoleUri);
              return [...result, ltiRoleRow(ltiRoleUri, ltiRoleName, oEQRoles)];
            }),
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );

  const handleRoleSearchOnSelect = (
    selectedOeqRoles: Set<OEQ.UserQuery.RoleDetails>,
  ) =>
    pipe(
      rolesMapping,
      // get current oEQ roles set for current lti role
      M.lookup(S.Eq)(selectedLtiRole),
      O.getOrElse<Set<OEQ.UserQuery.RoleDetails>>(() => new Set()),
      // add selected role to role set
      (originalRoles) =>
        pipe(originalRoles, SET.union(eqRoleById)(selectedOeqRoles)),
      // replace old oeq role set in map
      (newRoles) =>
        pipe(rolesMapping, M.upsertAt(S.Eq)(selectedLtiRole, newRoles)),
      setRolesMapping,
    );

  const handleOnConfirm = () => {
    onClose(rolesMapping);
  };

  const handleOnCancel = () => {
    onClose();
    setRolesMapping(value);
  };

  const selectLtiRole = (
    <>
      <Typography variant="h6" gutterBottom>
        {customRoleSelectLtiRole}
      </Typography>
      <LtiRoleSelect />
    </>
  );

  const selectOeqRole = (
    <>
      <StyledTypography variant="h6" gutterBottom>
        {customRoleSelectOeqRole}
      </StyledTypography>
      <RoleSearch
        mode={{
          type: "one_click",
          onAdd: (role: OEQ.UserQuery.RoleDetails) =>
            pipe(role, SET.singleton, handleRoleSearchOnSelect),
        }}
        onSelectAll={(roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) =>
          pipe(roles, RS.toSet, handleRoleSearchOnSelect)
        }
        search={roleListProvider}
        listHeight={300}
      />
    </>
  );

  return (
    <ConfirmDialog
      title={customRoleDialogTitle}
      open={open}
      confirmButtonText={okLabel}
      onConfirm={handleOnConfirm}
      onCancel={handleOnCancel}
      maxWidth="lg"
    >
      <Grid container>
        <Grid container item xs direction="column" rowSpacing={2}>
          <Grid item>{selectLtiRole}</Grid>
          <Grid item>{selectOeqRole}</Grid>
        </Grid>

        <StyledDivider orientation="vertical" flexItem light />

        <Grid container item xs rowSpacing={2} direction="column">
          <Grid item>
            <Typography variant="h6" gutterBottom>
              {customRoleCurrentSelections}
            </Typography>
            {M.isEmpty(rolesMapping) ? (
              <ListItem>
                <ListItemIcon>
                  <ErrorOutline />
                </ListItemIcon>
                <ListItemText secondary={customRoleAddRoles} />
              </ListItem>
            ) : (
              ltiOeqRoleMappingTable(rolesMapping)
            )}
          </Grid>

          {!M.isEmpty(rolesMapping) && (
            <Grid item>
              <Button
                color="secondary"
                onClick={() => setRolesMapping(new Map())}
                sx={{ float: "right" }}
              >
                {removeAllLabel}
              </Button>
            </Grid>
          )}
        </Grid>
      </Grid>
    </ConfirmDialog>
  );
};

export default SelectCustomRoleDialog;
