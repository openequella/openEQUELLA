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
  Divider,
  Icon,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useHistory } from "react-router";
import { getOeqTheme } from "../modules/ThemeModule";

interface MainMenuProps {
  /**
   * Groups of menu items to display in the menu - typically separated by `Divider`s. These groups
   * are normally computed on the server and based on the current user.
   */
  menuGroups: Array<Array<OEQ.LegacyContent.MenuItem>>;
  /**
   * Event handler for when a menu item is clicked - commonly used to show/hide the drawer which the
   * menu may be residing in at certain responsive break points.
   */
  onClickNavItem: () => void;
}

/**
 * A component responsible for the menu often found in the left hand side bar (or drawer) of the
 * main layout.
 */
const MainMenu = ({ menuGroups, onClickNavItem }: MainMenuProps) => {
  const menuColors = getOeqTheme().menu;

  const history = useHistory();
  const navItem = (item: OEQ.LegacyContent.MenuItem, ind: number) => (
    <ListItemButton
      component="a"
      href={item.href}
      target={item.newWindow ? "_blank" : "_self"}
      onClick={(e: React.MouseEvent<HTMLAnchorElement>) => {
        onClickNavItem();
        if (item.route) {
          e.preventDefault();
          history.push(item.route);
        }
      }}
      key={ind}
    >
      <ListItemIcon>
        {item.iconUrl ? (
          <img src={item.iconUrl} alt={item.title} />
        ) : (
          <Icon sx={{ color: menuColors.icon }}>
            {item.systemIcon ?? "folder"}
          </Icon>
        )}
      </ListItemIcon>
      <ListItemText
        disableTypography
        primary={
          <Typography
            variant="subtitle1"
            component="div"
            color={menuColors.text}
          >
            {item.title}
          </Typography>
        }
      />
    </ListItemButton>
  );

  return (
    <div id="menulinks">
      {menuGroups.map((group, index) => (
        <React.Fragment key={index}>
          {index > 0 && <Divider />}
          <List component="nav">{group.map(navItem)}</List>
        </React.Fragment>
      ))}
    </div>
  );
};

export default React.memo(MainMenu);
