import "babel-polyfill";
import { Theme, createMuiTheme } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { deepOrange, deepPurple } from "@material-ui/core/colors";

export const theme = createMuiTheme({
  palette: {
    primary: deepOrange,
    secondary: deepPurple
  },
  typography: {
    useNextVariants: true
  }
});

export const useStyles = makeStyles((theme: Theme) => {
  return {
    content: {
      display: "flex",
      flexDirection: "column",
      flexGrow: 1
    },
    root: {
      height: "100vh",
      display: "flex",
      flexDirection: "column"
    },
    body: {
      margin: theme.spacing.unit * 2
    },
    textField: {
      marginLeft: theme.spacing.unit,
      marginRight: theme.spacing.unit,
      width: 300
    }
  };
});
