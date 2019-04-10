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
import { cloudProviderLangStrings } from "./CloudProviderModule";
import { commonString } from "../util/commonstrings";

interface CloudProviderAddDialogProps {
  open: boolean;
  onCancel: () => void;
  onRegister: (url: string) => void;
}
interface CloudProviderAddDialogState {
  cloudProviderUrl: string;
  isUrlValid: boolean;
}

export default class CloudProviderAddDialog extends React.Component<
  CloudProviderAddDialogProps,
  CloudProviderAddDialogState
> {
  constructor(props: CloudProviderAddDialogProps) {
    super(props);
    this.state = {
      cloudProviderUrl: "",
      isUrlValid: true
    };
  }

  handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let url = e.target.value.toLowerCase();
    this.setState({
      cloudProviderUrl: url,
      isUrlValid: url.search(/^http(s?):\/\//) == 0
    });
  };

  render() {
    const { open, onCancel, onRegister } = this.props;
    const { cloudProviderUrl, isUrlValid } = this.state;
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
          <DialogTitle>
            {cloudProviderLangStrings.newcloudprovider.title}
          </DialogTitle>
          <DialogContent>
            <DialogContentText>
              {cloudProviderLangStrings.newcloudprovider.help}
            </DialogContentText>
            <TextField
              autoFocus
              margin="dense"
              id="new_cloud_provider_url"
              label={cloudProviderLangStrings.newcloudprovider.label}
              value={cloudProviderUrl}
              required
              fullWidth
              onChange={this.handleTextChange}
              error={!isUrlValid}
              helperText={cloudProviderLangStrings.newcloudprovider.error}
            />
          </DialogContent>
          <DialogActions>
            <Button id="cancel-register" onClick={onCancel} color="primary">
              {commonString.action.cancel}
            </Button>
            <Button
              id="confirm-register"
              onClick={() => {
                onRegister(cloudProviderUrl);
              }}
              color="primary"
              disabled={!cloudProviderUrl || !isUrlValid}
            >
              {commonString.action.register}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }
}
