import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from "@material-ui/core";
import TextField from "@material-ui/core/es/TextField";
import { langStrings } from "./CloudProviderModule";

interface CloudProviderAddDialogProps {
  open: boolean;
  onCancel: () => void;
  onRegister: () => void;
  getUrl: (url: string) => void;
}
interface CloudProviderAddDialogState {
  cloudProviderUrl: string;
  registerEnable: boolean;
}

export default class CloudProviderAddDialog extends React.Component<
  CloudProviderAddDialogProps,
  CloudProviderAddDialogState
> {
  constructor(props: CloudProviderAddDialogProps) {
    super(props);
    this.state = {
      cloudProviderUrl: "",
      registerEnable: true
    };
  }

  handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let cloudProviderUrl = e.target.value;
    this.setState({
      cloudProviderUrl: cloudProviderUrl,
      registerEnable: !cloudProviderUrl
    });
    this.props.getUrl(cloudProviderUrl);
  };

  render() {
    const { open, onCancel, onRegister } = this.props;
    const { registerEnable } = this.state;
    return (
      <div>
        <Dialog
          open={open}
          onClose={onCancel}
          aria-labelledby="form-dialog-title"
          disableBackdropClick={true}
          disableEscapeKeyDown={true}
          fullWidth
        >
          <DialogTitle id="form-dialog-title">
            {langStrings.newCloudProviderTitle}
          </DialogTitle>
          <DialogContent>
            <DialogContentText>
              {langStrings.newCloudProviderInfo.help}
            </DialogContentText>
            <TextField
              autoFocus
              margin="dense"
              id="new_cloud_provider_url"
              label="URL"
              value={this.state.cloudProviderUrl}
              required
              fullWidth
              onChange={this.handleTextChange}
            />
          </DialogContent>
          <DialogActions>
            <Button id="cancel-register" onClick={onCancel} color="primary">
              CANCEL
            </Button>
            <Button
              id="confirm-register"
              onClick={onRegister}
              color="primary"
              disabled={registerEnable}
            >
              REGISTER
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }
}
