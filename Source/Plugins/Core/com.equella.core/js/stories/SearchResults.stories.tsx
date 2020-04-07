import React from "react";
import { action } from "@storybook/addon-actions";
import { text } from "@storybook/addon-knobs";
import SearchResult from "../tsrc/components/SearchResult";

export default {
  title: "SearchResult",
  component: SearchResult
};

export const PrimaryTextOnly = () => (
  <SearchResult
    onClick={action("click")}
    onDelete={action("delete")}
    to={text("to", "#example")}
    primaryText={text(
      "primaryText",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
  />
);

export const PrimaryAndSecondaryText = () => (
  <SearchResult
    onClick={action("click")}
    onDelete={action("delete")}
    to={text("to", "#example")}
    primaryText={text(
      "primaryText",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    secondaryText={text(
      "secondaryText",
      "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"
    )}
  />
);
