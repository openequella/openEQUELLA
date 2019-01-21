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
import axios, {AxiosResponse} from "axios";
import {Config} from "../config";

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
  notice?: string
};

export const strings = prepLangStrings(
  "loginnoticepage", {
    title: "Login Notice Editor",
    label: "Login Notice"
  }
);

class LoginNoticeConfigPage extends React.Component<LoginNoticeConfigPageProps & WithStyles<typeof styles>, LoginNoticeConfigPageState> {

  constructor(props:LoginNoticeConfigPageProps&WithStyles<typeof styles>){
    super(props);

}
  state: LoginNoticeConfigPageState = {
    notice: ""
  };

  handleSubmitTheme = () => {
    axios.put(
      `${Config.baseUrl}api/loginnotice/settings/`,
      this.state.notice
    ).then(() => {
      window.location.reload();
    });
  };

  handleTextFieldChange = (e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement) => {
    this.setState({notice: e.value});
  };

  componentDidMount = () => {
    axios.get(`${Config.baseUrl}api/loginnotice/settings/`,
    ).then((response: AxiosResponse) => {
      this.setState({notice: response.data});
    });
  };

  render() {
    const styles = this.props.classes;
    const {Template} = this.props.bridge;
    return (
      <Template title={strings.title}>
        <Card className={styles.card} raised>
          <TextField className={styles.input} label={strings.label} rows={10} variant={"outlined"} multiline autoFocus
                     placeholder={"<div></div>"} onChange={e => this.handleTextFieldChange(e.target)}
                     value={this.state.notice}/>
          <CardActions className={styles.cardActions}>
            <Button onClick={this.handleSubmitTheme} className={styles.button} variant={"contained"}>Apply</Button>
          </CardActions>
        </Card>
      </Template>);
  }
}

export default withStyles(styles)(LoginNoticeConfigPage);
