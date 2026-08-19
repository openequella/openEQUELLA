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
import { Alert, Box, Button, List, Stack } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext } from "react";
import { Link } from "react-router-dom";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import {
  getMyResourceCategories,
  MyResourcesCategory,
} from "../../modules/MyResourceModule";
import { languageStrings } from "../../util/langstrings";
import { DraggablePortlet } from "../components/DraggablePortlet";
import { MyResourcesListItem } from "./MyResourcesListItem";
import type { PortletBasicProps } from "./PortletHelper";

const { showAll: showAllText } = languageStrings.common.action;

export interface PortletMyResourcesProps extends PortletBasicProps {
  /** A provider function to fetch the list of My Resources categories. Primarily for testing. */
  myResourcesTypeProvider?: typeof getMyResourceCategories;
}

/**
 * Portlet component that displays a list of the current user's resources type, displaying the status
 * (e.g. Published, Drafts, Scrapbook) and count.
 */
export const PortletMyResources: React.FC<PortletMyResourcesProps> = ({
  cfg,
  myResourcesTypeProvider = getMyResourceCategories,
  ...restProps
}) => {
  const { currentUser } = useContext(AppContext);

  const [isLoading, setIsLoading] = React.useState(true);
  const [errorMessage, setErrorMessage] = React.useState<string | undefined>(
    undefined,
  );
  const [myResourcesTypes, setMyResourcesTypes] = React.useState<
    MyResourcesCategory[]
  >([]);

  React.useEffect(() => {
    if (currentUser) {
      const fetchMyResourcesTypes = TE.tryCatch(
        () => myResourcesTypeProvider(currentUser.scrapbookEnabled),
        String,
      );

      pipe(
        fetchMyResourcesTypes,
        TE.match(setErrorMessage, setMyResourcesTypes),
        T.tapIO(() => () => setIsLoading(false)),
      )();
    }
  }, [currentUser, myResourcesTypeProvider]);

  const myResourcesTypesList = (
    <List>
      {myResourcesTypes.map((type) => (
        <MyResourcesListItem key={type.id} myResourcesType={type} />
      ))}
    </List>
  );

  return (
    <DraggablePortlet portlet={cfg} isLoading={isLoading} {...restProps}>
      <Stack spacing={2}>
        {errorMessage ? (
          <Alert severity="error">{errorMessage}</Alert>
        ) : (
          myResourcesTypesList
        )}
        <Box sx={{ justifyContent: "center", display: "flex" }}>
          <Button
            aria-label={showAllText}
            variant="outlined"
            component={Link}
            to={routes.MyResources.to("All resources")}
          >
            {showAllText}
          </Button>
        </Box>
      </Stack>
    </DraggablePortlet>
  );
};
