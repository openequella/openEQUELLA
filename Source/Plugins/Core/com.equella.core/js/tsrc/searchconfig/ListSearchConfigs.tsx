import { Bridge } from "../api/bridge";
import { WithStyles } from "@material-ui/core";
import * as React from "react";
import EntityList from "../components/EntityList";
import { SearchResult, handleUnexpectedApiError } from "../components";
import { prepLangStrings, formatSize } from "../util/langstrings";
import { SearchConfig, listAllConfigs } from ".";
import { ErrorResponse } from "../api/errors";

interface SearchConfigsProps extends WithStyles {
  bridge: Bridge;
}

interface SearchConfigsState {
  searchConfigs: SearchConfig[] | null;
  errorResponse?: ErrorResponse;
}

export const strings = prepLangStrings("searchconfigs", {
  title: "Search Configurations",
  configsAvailable: {
    zero: "No search configurations available",
    one: "%d configuration",
    more: "%d configurations"
  }
});

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
  }

  add = () => {
    return;
  };

  render() {
    const { Template } = this.props.bridge;
    const { searchConfigs, errorResponse } = this.state;
    return (
      <Template title={strings.title} errorResponse={errorResponse}>
        <EntityList
          progress={!searchConfigs}
          resultsText={formatSize(
            searchConfigs ? searchConfigs.length : 0,
            strings.configsAvailable
          )}
          createLink={{ href: "PLACEHOLDER", onClick: this.add }}
        >
          {!searchConfigs
            ? null
            : searchConfigs.map(sc => (
                <SearchResult
                  href="PLACEHOLDER"
                  onClick={e => e}
                  primaryText={sc.id}
                />
              ))}
        </EntityList>
      </Template>
    );
  }
}

export default ListSearchConfigs;
