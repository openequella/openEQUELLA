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
import { WithStyles } from "@material-ui/core";
import * as React from "react";
import EntityList from "../components/EntityList";
import { SearchResult, handleUnexpectedApiError } from "../components";
import { formatSize, languageStrings } from "../util/langstrings";
import { SearchConfig, listAllConfigs } from ".";
import { TemplateUpdateProps, templateDefaults } from "../mainui/Template";
import { routes } from "../mainui/routes";

interface SearchConfigsProps extends TemplateUpdateProps, WithStyles {}

interface SearchConfigsState {
  searchConfigs: SearchConfig[] | null;
}

export const strings = languageStrings.searchconfigs;

class ListSearchConfigs extends React.Component<
  SearchConfigsProps,
  SearchConfigsState
> {
  constructor(props: SearchConfigsProps) {
    super(props);
    this.state = {
      searchConfigs: null,
    };
  }

  componentDidMount() {
    listAllConfigs()
      .then((searchConfigs) => this.setState({ searchConfigs }))
      .catch(handleUnexpectedApiError(this));
    this.props.updateTemplate(templateDefaults(strings.title));
  }

  add = () => {
    return;
  };

  render() {
    const { searchConfigs } = this.state;
    return (
      <EntityList
        progress={!searchConfigs}
        resultsText={formatSize(
          searchConfigs ? searchConfigs.length : 0,
          strings.configsAvailable
        )}
        createOnClick={this.add}
      >
        {!searchConfigs
          ? null
          : searchConfigs.map((sc) => (
              <SearchResult
                to={routes.Search.path}
                onClick={(e) => e}
                primaryText={sc.name}
              />
            ))}
      </EntityList>
    );
  }
}

export default ListSearchConfigs;
