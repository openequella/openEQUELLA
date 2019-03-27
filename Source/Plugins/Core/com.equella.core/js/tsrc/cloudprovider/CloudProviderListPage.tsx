import { Bridge } from "../api/bridge";
import * as React from "react";
import {
  Avatar,
  createStyles,
  IconButton,
  Theme,
  withStyles,
  WithStyles
} from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import CloudIcon from "@material-ui/icons/CloudCircleRounded";
import { CloudProviderEntity } from "./CloudProviderEntity";
import {
  deleteCloudProvider,
  getCloudProviders,
  langStrings,
  registerCloudProviderInit
} from "./CloudProviderModule";
import { AxiosError } from "axios";
import { ErrorResponse, generateFromError } from "../api/errors";
import EntityList from "../components/EntityList";
import { formatSize } from "../util/langstrings";
import { sprintf } from "sprintf-js";
import ConfirmDialog from "../components/ConfirmDialog";
import CloudProviderAddDialog from "./CloudProviderAddDialog";
import EquellaListItem from "../components/EquellaListItem";

const styles = (theme: Theme) =>
  createStyles({
    searchResultContent: {
      marginTop: theme.spacing.unit
    },
    cloudIcon: {
      width: 40,
      height: 40
    }
  });

interface CloudProviderListPageProps extends WithStyles<typeof styles> {
  bridge: Bridge;
}
interface CloudProviderListPageState {
  cloudProviders: CloudProviderEntity[];
  error?: ErrorResponse;
  deleteDialogOpen: boolean;
  registerDialogOpen: boolean;
  deleteDetails?: CloudProviderEntity;
  cloudProviderUrl: string;
}

class CloudProviderListPage extends React.Component<
  CloudProviderListPageProps,
  CloudProviderListPageState
> {
  constructor(props: CloudProviderListPageProps) {
    super(props);
    this.state = {
      cloudProviders: [],
      deleteDialogOpen: false,
      registerDialogOpen: false,
      cloudProviderUrl: ""
    };
  }

  componentDidMount(): void {
    this.getCloudProviderList();
  }

  getCloudProviderList = () => {
    getCloudProviders()
      .then(result => {
        this.setState(prevState => ({
          cloudProviders: result.results
        }));
      })
      .catch((error: AxiosError) => {
        this.handleError(error);
      });
  };

  handleError = (error: Error) => {
    this.setState({
      error: generateFromError(error)
    });
  };

  deleteCloudProvider = (cloudProvider: CloudProviderEntity) => {
    this.setState({
      deleteDialogOpen: true,
      deleteDetails: cloudProvider
    });
  };
  cancelDeleteCloudProvider = () => {
    this.setState({
      deleteDialogOpen: false
    });
  };

  confirmDeleteCloudProvider = () => {
    if (this.state.deleteDetails) {
      let id = this.state.deleteDetails.id;
      this.cancelDeleteCloudProvider();
      deleteCloudProvider(id)
        .then(() => {
          this.getCloudProviderList();
        })
        .catch((error: AxiosError) => {
          this.handleError(error);
        });
    }
  };

  registerCloudProvider = () => {
    this.setState({
      registerDialogOpen: true
    });
  };

  cancelRegisterCloudProvider = () => {
    this.setState({
      registerDialogOpen: false
    });
  };

  confirmRegisterCloudProvider = () => {
    let cloudProviderUrl = this.state.cloudProviderUrl;
    registerCloudProviderInit(cloudProviderUrl)
      .then(result => {
        window.location.href = result.url;
      })
      .catch((error: AxiosError) => {
        this.setState({
          error: generateFromError(error)
        });
      });
  };

  getUrlFromDialog = (url: string) => {
    this.setState({
      cloudProviderUrl: url
    });
  };

  render() {
    const { Template } = this.props.bridge;
    const {
      error,
      cloudProviders,
      deleteDialogOpen,
      registerDialogOpen
    } = this.state;
    //open a dialog instead of going to another page
    const registerLink = {
      href: "",
      onClick: this.registerCloudProvider
    };
    return (
      <Template title={langStrings.title} errorResponse={error}>
        {this.state.deleteDetails && (
          <ConfirmDialog
            open={deleteDialogOpen}
            title={sprintf(
              langStrings.deleteCloudProviderTitle,
              this.state.deleteDetails.name
            )}
            onConfirm={this.confirmDeleteCloudProvider}
            onCancel={this.cancelDeleteCloudProvider}
          >
            {langStrings.deleteCloudProviderMsg}
          </ConfirmDialog>
        )}
        <CloudProviderAddDialog
          open={registerDialogOpen}
          onCancel={this.cancelRegisterCloudProvider}
          onRegister={this.confirmRegisterCloudProvider}
          getUrl={this.getUrlFromDialog}
        />
        <EntityList
          id="cloudProviderList"
          resultsText={formatSize(
            cloudProviders.length,
            langStrings.cloudProviderAvailable
          )}
          progress={false}
          createLink={registerLink}
        >
          {cloudProviders.map(cloudProvider => {
            let secondaryAction = (
              <IconButton
                onClick={() => {
                  this.deleteCloudProvider(cloudProvider);
                }}
              >
                <DeleteIcon />
              </IconButton>
            );
            let icon = (
              <Avatar
                src={cloudProvider.iconUrl}
                alt={cloudProvider.description}
              >
                {!cloudProvider.iconUrl && (
                  <CloudIcon className={this.props.classes.cloudIcon} />
                )}
              </Avatar>
            );

            return (
              <EquellaListItem
                key={cloudProvider.id}
                listItemPrimaryText={cloudProvider.name}
                listItemSecondText={cloudProvider.description}
                listItemAttributes={{ divider: true }}
                icon={icon}
                secondaryAction={secondaryAction}
              />
            );
          })}
        </EntityList>
      </Template>
    );
  }
}

export default withStyles(styles)(CloudProviderListPage);
