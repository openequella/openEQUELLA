import * as React from "react";
import {
  Avatar,
  IconButton,
  List,
  ListItem,
  ListItemAvatar,
  ListItemSecondaryAction,
  ListItemText,
  Paper,
  Typography
} from "@material-ui/core/";
import { WithStyles } from "@material-ui/core/styles";
import withStyles from "@material-ui/core/styles/withStyles";
import { CloudProviderEntity } from "./CloudProviderEntity";
import DeleteIcon from "@material-ui/icons/Delete";
import { formatSize } from "../util/langstrings";
import { getCloudProviderListStyle, langStrings } from "./CloudProviderModule";

const styles = getCloudProviderListStyle();

interface CloudProviderListProps extends WithStyles<typeof styles> {
  cloudProviders: CloudProviderEntity[];
}

class CloudProviderList extends React.Component<CloudProviderListProps> {
  render() {
    const { classes, cloudProviders } = this.props;
    return (
      <div className={classes.overall}>
        <Paper className={classes.results}>
          <div className={classes.resultHeader}>
            <Typography className={classes.resultText} variant="subtitle1">
              {formatSize(
                cloudProviders.length,
                langStrings.cloudProviderAvailable
              )}
            </Typography>
          </div>
          <List>
            {cloudProviders.map(cloudProvider => {
              let primaryText = (
                <Typography color="primary" variant="subtitle1">
                  {cloudProvider.name}
                </Typography>
              );
              let secondaryText = (
                <Typography
                  variant="body1"
                  className={classes.searchResultContent}
                >
                  {cloudProvider.description}
                </Typography>
              );
              return (
                <ListItem button divider key={cloudProvider.id}>
                  <ListItemAvatar>
                    <Avatar
                      alt={cloudProvider.description}
                      src={cloudProvider.iconUrl}
                    />
                  </ListItemAvatar>
                  <ListItemText
                    disableTypography={true}
                    className={classes.searchResultContent}
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
          </List>
        </Paper>
      </div>
    );
  }
}

export default withStyles(styles)(CloudProviderList);
