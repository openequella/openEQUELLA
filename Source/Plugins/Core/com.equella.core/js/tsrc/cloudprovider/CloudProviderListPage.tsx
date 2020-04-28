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
  registerCloudProviderInit,
  refreshCloudProvider
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
import { commonString } from "../util/commonstrings";
import MessageInfo from "../components/MessageInfo";

const styles = (theme: Theme) =>
  createStyles({
    searchResultContent: {
      marginTop: theme.spacing(1)
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
  showRefreshed: boolean;
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
      showRefreshed: false
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

  refreshProvider = (cloudProvider: CloudProviderEntity) => {
    refreshCloudProvider(cloudProvider.id).then(_ => {
      this.getCloudProviderList();
      this.setState({ showRefreshed: true });
    });
  };

  cancelDeleteCloudProvider = () => {
    this.setState({
      deleteDialogOpen: false
    });
  };

  confirmDeleteCloudProvider = () => {
    if (this.state.deleteDetails) {
      const id = this.state.deleteDetails.id;
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
        <MessageInfo
          variant="success"
          open={this.state.showRefreshed}
          title={cloudProviderLangStrings.refreshed}
          onClose={() => this.setState({ showRefreshed: false })}
        />
        {this.state.deleteDetails && (
          <ConfirmDialog
            open={deleteDialogOpen}
            title={sprintf(
              cloudProviderLangStrings.deletecloudprovider.title,
              this.state.deleteDetails.name
            )}
            onConfirm={this.confirmDeleteCloudProvider}
            onCancel={this.cancelDeleteCloudProvider}
            confirmButtonText={commonString.action.delete}
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
            const secondaryAction = (
              <IconButton
                onClick={() => {
                  this.deleteCloudProvider(cloudProvider);
                }}
              >
                <DeleteIcon />
              </IconButton>
            );
            const icon = (
              <Avatar
                src={cloudProvider.iconUrl}
                alt={cloudProvider.description}
              >
                {!cloudProvider.iconUrl && (
                  <CloudIcon className={this.props.classes.cloudIcon} />
                )}
              </Avatar>
            );

            const secondaryText = cloudProvider.canRefresh ? (
              <>
                {cloudProvider.description} -{" "}
                <a
                  href="javascript:void"
                  onClick={_ => this.refreshProvider(cloudProvider)}
                >
                  {commonString.action.refresh}
                </a>
              </>
            ) : (
              cloudProvider.description
            );
            return (
              <EquellaListItem
                key={cloudProvider.id}
                listItemPrimaryText={cloudProvider.name}
                listItemSecondText={secondaryText}
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
