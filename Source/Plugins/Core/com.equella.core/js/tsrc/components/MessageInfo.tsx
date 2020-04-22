import {
  IconButton,
  Snackbar,
  SnackbarContent,
  Theme,
} from "@material-ui/core";
import amber from "@material-ui/core/colors/amber";
import green from "@material-ui/core/colors/green";
import { WithStyles, createStyles, withStyles } from "@material-ui/core/styles";
import CheckCircleIcon from "@material-ui/icons/CheckCircle";
import CloseIcon from "@material-ui/icons/Close";
import ErrorIcon from "@material-ui/icons/Error";
import InfoIcon from "@material-ui/icons/Info";
import WarningIcon from "@material-ui/icons/Warning";
import * as React from "react";
import { commonString } from "../util/commonstrings";

const variantIcon = {
  success: CheckCircleIcon,
  warning: WarningIcon,
  error: ErrorIcon,
  info: InfoIcon,
};

const styles = (theme: Theme) =>
  createStyles({
    success: {
      backgroundColor: green[600],
    },
    error: {
      backgroundColor: theme.palette.error.dark,
    },
    info: {
      backgroundColor: theme.palette.primary.dark,
    },
    warning: {
      backgroundColor: amber[700],
    },
    icon: {
      fontSize: 20,
    },
    iconVariant: {
      opacity: 0.9,
      marginRight: theme.spacing(1),
    },
    message: {
      display: "flex",
      alignItems: "center",
    },
  });

interface MessageInfoProps {
  open: boolean;
  onClose: () => void;
  title: string;
  variant: "success" | "warning" | "error" | "info";
}

export default withStyles(styles)(
  class MessageInfo extends React.Component<
    MessageInfoProps & WithStyles<typeof styles>
  > {
    render() {
      const { open, title, onClose, classes, variant } = this.props;
      const Icon = variantIcon[variant];
      return (
        <Snackbar open={open} onClose={onClose} autoHideDuration={5000}>
          <SnackbarContent
            className={classes[variant]}
            aria-describedby="client-snackbar"
            message={
              <span id="client-snackbar" className={classes.message}>
                <Icon className={`${classes.icon} ${classes.iconVariant}`} />
                {title}
              </span>
            }
            action={
              <IconButton
                key="close"
                aria-label={commonString.action.close}
                color="inherit"
                onClick={onClose}
              >
                <CloseIcon className={classes.icon} />
              </IconButton>
            }
          />
        </Snackbar>
      );
    }
  }
);
