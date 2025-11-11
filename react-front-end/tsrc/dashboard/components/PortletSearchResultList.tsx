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
import { List, ListItem, ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { Link } from "react-router-dom";
import OEQThumb from "../../components/OEQThumb";
import { routes } from "../../mainui/routes";

// CSS styles for two-line text truncation
const descriptionStyles = {
  display: "-webkit-box",
  WebkitLineClamp: 2,
  WebkitBoxOrient: "vertical",
  overflow: "hidden",
  textOverflow: "ellipsis",
  wordBreak: "break-word",
};

export interface PortletSearchResultListProps {
  /** Array of search result items to display */
  results: OEQ.Search.SearchResultItem[];
  /** If true, hides the description field and displays title only */
  hideDescription?: boolean;
}

/**
 * Component that displays a minimal list of search results suitable for Portlets.
 */
export const PortletSearchResultList = ({
  results,
  hideDescription = false,
}: PortletSearchResultListProps) => {
  return (
    <List>
      {results.map((item) => (
        <ListItem
          key={item.uuid + ":" + item.version}
          component={Link}
          to={routes.ViewItem.to(item.uuid, item.version)}
        >
          <OEQThumb details={item.thumbnailDetails} />
          <ListItemText
            primary={item.name || item.uuid}
            secondary={!hideDescription ? item.description : null}
            sx={{
              "& .MuiListItemText-secondary": descriptionStyles,
            }}
          />
        </ListItem>
      ))}
    </List>
  );
};
