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
      searchConfigs: null
    };
  }

  componentDidMount() {
    listAllConfigs()
      .then(searchConfigs => this.setState({ searchConfigs }))
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
          : searchConfigs.map(sc => (
              <SearchResult
                to={routes.Search.path}
                onClick={e => e}
                primaryText={sc.name}
              />
            ))}
      </EntityList>
    );
  }
}

export default ListSearchConfigs;
