import * as React from "react";
import { Bridge } from "../api/bridge";
import {
  Button,
  Grid,
  Paper,
  TextField,
  Theme,
  withStyles,
  WithStyles
} from "@material-ui/core";
import createStyles from "@material-ui/core/es/styles/createStyles";
import { ErrorResponse, generateFromError } from "../api/errors";
import { langStrings, registerCloudProviderInit } from "./CloudProviderModule";
import { commonString } from "../util/commonstrings";
import { AxiosError } from "axios";

const styles = (theme: Theme) =>
  createStyles({
    footer: {
      minHeight: 48
    },
    footerActions: {
      padding: "4px",
      paddingRight: "20px",
      display: "flex",
      justifyContent: "flex-end"
    },
    form: {
      display: "flex",
      flexFlow: "row wrap"
    },
    formControl2: {
      margin: theme.spacing.unit,
      flex: "2 1 100%",
      marginBottom: 2 * theme.spacing.unit
    },
    body: {
      padding: `${theme.spacing.unit * 2}px`,
      paddingBottom: 48,
      height: "100%"
    }
  });

interface NewCloudProviderProps extends WithStyles<typeof styles> {
  bridge: Bridge;
}
interface NewCloudProviderState {
  error?: ErrorResponse;
  cloudProviderUrl: string;
}

class NewCloudProvider extends React.Component<
  NewCloudProviderProps,
  NewCloudProviderState
> {
  constructor(props: NewCloudProviderProps) {
    super(props);
    this.state = {
      error: undefined,
      cloudProviderUrl: ""
    };
  }

  handleRegisterCloudProvider = () => {
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

  handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({
      cloudProviderUrl: e.target.value
    });
  };

  render() {
    const { router, routes, Template } = this.props.bridge;
    const { classes } = this.props;
    const { error, cloudProviderUrl } = this.state;
    const saveOrCancel = (
      <Paper className={classes.footerActions}>
        <Button
          onClick={router(routes.CloudProviderListPage).onClick}
          color="secondary"
        >
          {commonString.action.cancel}
        </Button>
        <Button
          id="register-cloud-provider"
          onClick={this.handleRegisterCloudProvider}
          color="primary"
          disabled={!cloudProviderUrl}
        >
          {commonString.action.register}
        </Button>
      </Paper>
    );

    return (
      <Template
        title={langStrings.newCloudProviderTitle}
        fixedViewPort={true}
        backRoute={routes.CloudProviderListPage}
        footer={saveOrCancel}
        errorResponse={error}
      >
        <Grid>
          <div className={classes.form}>
            <TextField
              id={langStrings.newCloudProviderInfo.id}
              label={langStrings.newCloudProviderInfo.label}
              helperText={langStrings.newCloudProviderInfo.help}
              margin="normal"
              value={cloudProviderUrl}
              className={classes.formControl2}
              required
              onChange={this.handleTextChange}
            />
          </div>
        </Grid>
      </Template>
    );
  }
}

export default withStyles(styles)(NewCloudProvider);
