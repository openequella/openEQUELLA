import { Bridge } from "../api/bridge";
import * as React from "react";
import {
  Avatar,
  createStyles,
  IconButton,
  ListItem,
  ListItemAvatar,
  ListItemSecondaryAction,
  ListItemText,
  Theme,
  Typography,
  withStyles,
  WithStyles
} from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import CloudIcon from "@material-ui/icons/CloudCircleRounded";
import { CloudProviderEntity } from "./CloudProviderEntity";
import {
  deleteCloudProvider,
  getCloudProviders,
  langStrings
} from "./CloudProviderModule";
import { AxiosError } from "axios";
import { ErrorResponse, generateFromError } from "../api/errors";
import EntityList from "../components/EntityList";
import { formatSize } from "../util/langstrings";
import { sprintf } from "sprintf-js";
import ConfirmDialog from "../components/ConfirmDialog";

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
  confirmOpen: boolean;
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
      confirmOpen: false
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

  handleDelete = (cloudProvider: CloudProviderEntity) => {
    this.setState({
      confirmOpen: true,
      deleteDetails: cloudProvider
    });
  };

  handleCancel = () => {
    this.setState({
      confirmOpen: false
    });
  };

  deleteCloudProvider = () => {
    if (this.state.deleteDetails) {
      let id = this.state.deleteDetails.id;
      this.handleCancel();
      deleteCloudProvider(id)
        .then(() => {
          this.getCloudProviderList();
        })
        .catch((error: AxiosError) => {
          this.handleError(error);
        });
    }
  };

  render() {
    const { Template, routes, router } = this.props.bridge;
    const { error, cloudProviders, confirmOpen } = this.state;
    return (
      <Template title={langStrings.title} errorResponse={error}>
        {this.state.deleteDetails && (
          <ConfirmDialog
            open={confirmOpen}
            title={sprintf(
              langStrings.deleteCloudProviderTitle,
              this.state.deleteDetails.name
            )}
            onConfirm={() => {
              this.deleteCloudProvider();
            }}
            onCancel={() => {
              this.handleCancel();
            }}
          >
            {langStrings.deleteCloudProviderMsg}
          </ConfirmDialog>
        )}

        <EntityList
          id="cloudProviderList"
          resultsText={formatSize(
            cloudProviders.length,
            langStrings.cloudProviderAvailable
          )}
          progress={false}
          createLink={router(routes.NewCloudProvider)}
        >
          {cloudProviders.map(cloudProvider => {
            let primaryText = (
              <Typography color="primary" variant="subtitle1">
                {cloudProvider.name}
              </Typography>
            );
            let secondaryText = (
              <Typography
                variant="body1"
                className={this.props.classes.searchResultContent}
              >
                {cloudProvider.description}
              </Typography>
            );
            return (
              <ListItem button divider key={cloudProvider.id}>
                <ListItemAvatar>
                  {
                    <Avatar
                      src={cloudProvider.iconUrl}
                      alt={cloudProvider.description}
                    >
                      {!cloudProvider.iconUrl && (
                        <CloudIcon className={this.props.classes.cloudIcon} />
                      )}
                    </Avatar>
                  }
                </ListItemAvatar>
                <ListItemText
                  disableTypography={true}
                  primary={primaryText}
                  secondary={secondaryText}
                />
                <ListItemSecondaryAction>
                  <IconButton
                    onClick={() => {
                      this.handleDelete(cloudProvider);
                    }}
                  >
                    <DeleteIcon />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            );
          })}
        </EntityList>
      </Template>
    );
  }
}

export default withStyles(styles)(CloudProviderListPage);
