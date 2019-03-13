import { Bridge } from "../api/bridge";
import * as React from "react";
import { Fab, withStyles, WithStyles } from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";
import CloudProviderList from "./CloudProviderList";
import { CloudProviderEntity } from "./CloudProviderEntity";
import {
  getCloudProviderListPageStyle,
  getCloudProviders,
  langStrings
} from "./CloudProviderModule";
import { AxiosError } from "axios";
import { ErrorResponse, generateFromError } from "../api/errors";

const styles = getCloudProviderListPageStyle();

interface CloudProviderListPageProps extends WithStyles<typeof styles> {
  bridge: Bridge;
}
interface CloudProviderListPageState {
  cloudProviders: CloudProviderEntity[];
  error?: ErrorResponse;
}

class CloudProviderListPage extends React.Component<
  CloudProviderListPageProps,
  CloudProviderListPageState
> {
  constructor(props: CloudProviderListPageProps) {
    super(props);
    this.state = {
      cloudProviders: [],
      error: undefined
    };
  }

  componentDidMount(): void {
    getCloudProviders()
      .then(result => {
        this.setState(prevState => ({
          cloudProviders: result.results
        }));
      })
      .catch((error: AxiosError) => {
        this.setState({
          error: generateFromError(error)
        });
      });
  }

  render() {
    const { Template } = this.props.bridge;
    const { cloudProviders, error } = this.state;
    return (
      <Template title={langStrings.title} errorResponse={error}>
        <CloudProviderList cloudProviders={cloudProviders} />
        <Fab className={this.props.classes.fab} color="secondary">
          <AddIcon />
        </Fab>
      </Template>
    );
  }
}

export default withStyles(styles)(CloudProviderListPage);
