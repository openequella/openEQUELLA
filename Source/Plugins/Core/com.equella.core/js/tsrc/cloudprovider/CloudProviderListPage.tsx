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
import { getCloudProviders, langStrings } from "./CloudProviderModule";
import { AxiosError } from "axios";
import { ErrorResponse, generateFromError } from "../api/errors";
import EntityList from "../components/EntityList";
import { formatSize } from "../util/langstrings";

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
}

class CloudProviderListPage extends React.Component<
  CloudProviderListPageProps,
  CloudProviderListPageState
> {
  constructor(props: CloudProviderListPageProps) {
    super(props);
    this.state = {
      cloudProviders: []
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
    const { error, cloudProviders } = this.state;
    const createLink = {
      href: "",
      onClick: () => {}
    };
    return (
      <Template title={langStrings.title} errorResponse={error}>
        <EntityList
          resultsText={formatSize(
            cloudProviders.length,
            langStrings.cloudProviderAvailable
          )}
          progress={false}
          createLink={createLink}
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
                  <IconButton>
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
