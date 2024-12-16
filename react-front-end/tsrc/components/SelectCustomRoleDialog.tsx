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
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  TextField,
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
import { constVoid, flow, pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import * as SET from "fp-ts/Set";
import { useEffect, useState } from "react";
import * as React from "react";
import { isNonEmptyString } from "../util/validation";
import ConfirmDialog from "./ConfirmDialog";
import {
  CustomRole,
  customRoleEq,
  customRoleOrd,
  CustomRolesMapping,
} from "./CustomRoleHelper";
import RoleSearch from "./securityentitysearch/RoleSearch";
import SecurityEntityEntry from "./securityentitydialog/SecurityEntityEntry";
import { eqRoleById, ordRole } from "../modules/RoleModule";
import { languageStrings } from "../util/langstrings";

const {
  title,
  customRoleColumn: defaultCustomRoleColumnName,
  customRoleLabel: customRoleLabelText,
  oeqRoleTitle,
  oeqRoleColumn,
  currentMappings,
  addRoles,
} = languageStrings.selectCustomRoleDialog;
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
  /** The initial roles mapping value. */
  initialRoleMappings: CustomRolesMapping;
  /** Handler for when dialog is closed. */
  onClose: (selections?: CustomRolesMapping) => void;
  /**
   * Strings used in the dialog.
   */
  strings?: {
    /** Dialog title. */
    title: string;
    /** Label for custom role control. */
    customRoleLabel: string;
    /** Name of custom role column in the role mappings table. */
    customRoleColumnName: string;
  };
  /**
   * Function to render a component that supports the input of a custom role.
   *
   * @param value - The value of the current selected role.
   * @param onChange - The function to call when the role is changed.
   */
  customRoleSelector?: (
    onChange: (newValue: CustomRole) => void,
    value?: CustomRole,
  ) => React.ReactNode;
  /**
   * Default role for the custom role field.
   */
  defaultCustomRole?: CustomRole;
  /** Function which will provide the list of Role (search function) for RoleSearch. */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * This dialog used for selecting custom role and searching for oEQ roles,
 * and making selections of oEQ roles. The search component and custom role selector is displayed on the left hand side,
 * from which a user can make selections which will then be added to a table on the right hand side.
 * When the user confirms the dialog (click's OK),
 * that roles mapping on the right hand side is returned to the calling component.
 */
const SelectCustomRoleDialog = ({
  open,
  initialRoleMappings,
  onClose,
  searchRoleProvider,
  strings = {
    title,
    customRoleLabel: customRoleLabelText,
    customRoleColumnName: defaultCustomRoleColumnName,
  },
  defaultCustomRole,
  customRoleSelector,
}: SelectCustomRoleDialogProps) => {
  const { title: dialogTitle, customRoleLabel, customRoleColumnName } = strings;
  const [selectedCustomRole, setSelectedCustomRole] =
    useState(defaultCustomRole);

  const [showErrorCustomRoleSelector, setShowErrorCustomRoleSelector] =
    useState(false);

  // state for final selections which will be returned to the calling component when the user clicks OK
  const [rolesMapping, setRolesMapping] =
    React.useState<CustomRolesMapping>(initialRoleMappings);

  useEffect(() => {
    setRolesMapping(initialRoleMappings);
  }, [initialRoleMappings]);

  // oEQ role entry in the `oEQ column`
  const oeqRoleEntry = (
    customRole: CustomRole,
    role: OEQ.UserQuery.RoleDetails,
  ) => (
    <SecurityEntityEntry
      key={role.id}
      name={role.name}
      onDelete={() =>
        pipe(
          rolesMapping,
          // get current set of oEQ roles for corresponding custom role
          M.lookup(customRoleEq)(customRole),
          // remove selected role
          O.map(flow(SET.remove(eqRoleById)(role))),
          // replace old oeq role set in map
          O.chain((newRoles) =>
            SET.isEmpty(newRoles)
              ? pipe(rolesMapping, M.deleteAt(customRoleEq)(customRole), O.of)
              : pipe(
                  rolesMapping,
                  M.updateAt(customRoleEq)(customRole, newRoles),
                ),
          ),
          O.map(setRolesMapping),
          O.getOrElse(() =>
            console.warn(`Can't find custom role: ${customRole} in map`),
          ),
        )
      }
    />
  );

  // the row of each custom role
  const customRoleRow = (
    customRole: CustomRole,
    roles: Set<OEQ.UserQuery.RoleDetails>,
  ) => {
    const { role, name } = customRole;
    return (
      <TableRow
        key={role}
        sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
      >
        <TableCell component="th" scope="row">
          {name ?? role}
        </TableCell>
        <TableCell align="right">
          <List>
            {pipe(
              roles,
              SET.toArray(ordRole),
              A.map((r) => oeqRoleEntry(customRole, r)),
            )}
          </List>
        </TableCell>
      </TableRow>
    );
  };

  // the table shows the mapping relationships between custom roles and oEQ roles
  const customOeqRoleMappingTable = (roleMaps: CustomRolesMapping) => (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>{customRoleColumnName}</TableCell>
            <TableCell>{oeqRoleColumn}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {pipe(
            roleMaps,
            M.reduceWithIndex(customRoleOrd)<
              React.JSX.Element[],
              Set<OEQ.UserQuery.RoleDetails>
            >([], (customRole, result, oEQRoles) => [
              ...result,
              customRoleRow(customRole, oEQRoles),
            ]),
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );

  const handleRoleSearchOnSelect = (
    selectedOeqRoles: Set<OEQ.UserQuery.RoleDetails>,
  ) =>
    pipe(
      selectedCustomRole,
      O.fromNullable,
      O.fold(constVoid, (customRole) => {
        pipe(
          rolesMapping,
          M.lookup(customRoleEq)(customRole),
          // get current oEQ roles set for current custom role
          O.getOrElse<Set<OEQ.UserQuery.RoleDetails>>(() => new Set()),
          // add selected role to role set
          flow(SET.union(eqRoleById)(selectedOeqRoles)),
          // replace old oeq role set in map
          (newRoles) =>
            pipe(rolesMapping, M.upsertAt(customRoleEq)(customRole, newRoles)),
          setRolesMapping,
        );
      }),
    );

  const handleOnConfirm = () => {
    onClose(rolesMapping);
  };

  const handleOnCancel = () => {
    onClose();
    setRolesMapping(initialRoleMappings);
  };

  const defaultCustomRoleSelector = (
    <TextField
      label={customRoleLabel}
      // Use empty string as default value to avoid uncontrolled warning
      value={selectedCustomRole?.role ?? ""}
      error={
        showErrorCustomRoleSelector &&
        !isNonEmptyString(selectedCustomRole?.role)
      }
      onChange={(event) =>
        setSelectedCustomRole({
          role: event.target.value,
        })
      }
      aria-label={customRoleLabel}
      variant="standard"
      fullWidth
    />
  );

  const selectOeqRole = (
    <>
      <StyledTypography variant="h6" gutterBottom>
        {oeqRoleTitle}
      </StyledTypography>
      <RoleSearch
        mode={{
          type: "one_click",
          onAdd: (role: OEQ.UserQuery.RoleDetails) => {
            setShowErrorCustomRoleSelector(true);
            if (isNonEmptyString(selectedCustomRole?.role)) {
              pipe(role, SET.singleton, handleRoleSearchOnSelect);
            }
          },
        }}
        onSelectAll={(roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) => {
          setShowErrorCustomRoleSelector(true);
          if (isNonEmptyString(selectedCustomRole?.role)) {
            pipe(roles, RS.toSet, handleRoleSearchOnSelect);
          }
        }}
        search={searchRoleProvider}
        listHeight={300}
      />
    </>
  );

  return (
    <ConfirmDialog
      title={dialogTitle}
      open={open}
      confirmButtonText={okLabel}
      onConfirm={handleOnConfirm}
      onCancel={handleOnCancel}
      maxWidth="lg"
    >
      <Grid container>
        <Grid container item xs direction="column" rowSpacing={2}>
          <Grid item>
            {customRoleSelector?.(setSelectedCustomRole, selectedCustomRole) ??
              defaultCustomRoleSelector}
          </Grid>
          <Grid item>{selectOeqRole}</Grid>
        </Grid>

        <StyledDivider orientation="vertical" flexItem sx={{ opacity: 0.6 }} />

        <Grid container item xs rowSpacing={2} direction="column">
          <Grid item>
            <Typography variant="h6" gutterBottom>
              {currentMappings}
            </Typography>
            {M.isEmpty(rolesMapping) ? (
              <ListItem>
                <ListItemIcon>
                  <ErrorOutline />
                </ListItemIcon>
                <ListItemText secondary={addRoles} />
              </ListItem>
            ) : (
              customOeqRoleMappingTable(rolesMapping)
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
