import * as React from "react";
import {Bridge} from "../api/bridge";
import {WithStyles} from "@material-ui/core";
import {prepLangStrings} from "../util/langstrings";
import Card from "@material-ui/core/Card";
import CardActions from "@material-ui/core/CardActions";
import Button from "@material-ui/core/Button";
import createStyles from "@material-ui/core/styles/createStyles";
import TextField from "@material-ui/core/TextField";
import withStyles from "@material-ui/core/styles/withStyles";

const styles = createStyles({
  card: {
    marginTop: "16px",
    marginBottom: "16px",
    overflow: "visible"
  },
  input: {
    margin: "16px",
    width: "500px"
  },
  cardActions: {
    marginBottom: "16px"
  },
  button: {
    marginTop: "8px",
    marginBottom: "8px"
  }
});

interface LoginNoticeConfigPageProps {
  bridge: Bridge;
};

interface LoginNoticeConfigPageState {
};

export const strings = prepLangStrings(
  "loginnoticepage", {
    title: "Login Notice Editor"
  }
);

class LoginNoticeConfigPage extends React.Component<LoginNoticeConfigPageProps & WithStyles<typeof styles>, LoginNoticeConfigPageState> {

  handleSubmitTheme = () => {

  };

  componentDidMount = () => {

  };

  render() {
    const styles = this.props.classes;
    const {Template} = this.props.bridge;
    return (
      <Template title={strings.title}>
        <Card className={styles.card} raised>
          <TextField className={styles.input} label={"Login Notice"} rows={10} variant={"outlined"} multiline autoFocus
                     placeholder={"<div></div>"}/>
          <CardActions className={styles.cardActions}>
            <Button className={styles.button} variant={"contained"}>Apply</Button>
          </CardActions>
        </Card>
      </Template>);
  }
}

export default withStyles(styles)(LoginNoticeConfigPage);
