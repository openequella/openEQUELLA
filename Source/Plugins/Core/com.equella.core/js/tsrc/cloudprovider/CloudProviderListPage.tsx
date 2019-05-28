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
  cloudProviderLangStrings,
  registerCloudProviderInit
} from "./CloudProviderModule";
import EntityList from "../components/EntityList";
import { formatSize } from "../util/langstrings";
import { sprintf } from "sprintf-js";
import ConfirmDialog from "../components/ConfirmDialog";
import CloudProviderAddDialog from "./CloudProviderAddDialog";
import EquellaListItem from "../components/EquellaListItem";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../mainui/Template";
import { generateFromError } from "../api/errors";

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

interface CloudProviderListPageProps
  extends TemplateUpdateProps,
    WithStyles<typeof styles> {}

interface CloudProviderListPageState {
  cloudProviders: CloudProviderEntity[];
  deleteDialogOpen: boolean;
  registerDialogOpen: boolean;
  deleteDetails?: CloudProviderEntity;
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
      registerDialogOpen: false
    };
  }

  handleError = (error: Error) => {
    this.props.updateTemplate(templateError(generateFromError(error)));
  };

  componentDidMount(): void {
    this.getCloudProviderList();
    this.props.updateTemplate(templateDefaults(cloudProviderLangStrings.title));
  }

  getCloudProviderList = () => {
    getCloudProviders()
      .then(result => {
        this.setState(prevState => ({
          cloudProviders: result.results
        }));
      })
      .catch(this.handleError);
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
        .catch(this.handleError);
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

  confirmRegisterCloudProvider = (url: string) => {
    registerCloudProviderInit(url)
      .then(result => {
        window.location.href = result.url;
      })
      .catch(this.handleError);
  };

  render() {
    const { cloudProviders, deleteDialogOpen, registerDialogOpen } = this.state;
    return (
      <>
        {this.state.deleteDetails && (
          <ConfirmDialog
            open={deleteDialogOpen}
            title={sprintf(
              cloudProviderLangStrings.deletecloudprovider.title,
              this.state.deleteDetails.name
            )}
            onConfirm={this.confirmDeleteCloudProvider}
            onCancel={this.cancelDeleteCloudProvider}
          >
            {cloudProviderLangStrings.deletecloudprovider.message}
          </ConfirmDialog>
        )}
        <CloudProviderAddDialog
          open={registerDialogOpen}
          onCancel={this.cancelRegisterCloudProvider}
          onRegister={this.confirmRegisterCloudProvider}
        />
        <EntityList
          id="cloudProviderList"
          resultsText={formatSize(
            cloudProviders.length,
            cloudProviderLangStrings.cloudprovideravailable
          )}
          progress={false}
          createOnClick={this.registerCloudProvider}
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
      </>
    );
  }
}

export default withStyles(styles)(CloudProviderListPage);
