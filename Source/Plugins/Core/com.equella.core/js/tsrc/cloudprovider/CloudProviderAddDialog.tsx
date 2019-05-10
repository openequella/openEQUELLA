import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField
} from "@material-ui/core";
import { cloudProviderLangStrings } from "./CloudProviderModule";
import { commonString } from "../util/commonstrings";

interface CloudProviderAddDialogProps {
  open: boolean;
  onCancel: () => void;
  onRegister: (url: string) => void;
}
interface CloudProviderAddDialogState {
  cloudProviderUrl: string;
}

export default class CloudProviderAddDialog extends React.Component<
  CloudProviderAddDialogProps,
  CloudProviderAddDialogState
> {
  constructor(props: CloudProviderAddDialogProps) {
    super(props);
    this.state = {
      cloudProviderUrl: ""
    };
  }

  handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({
      cloudProviderUrl: e.target.value
    });
  };

  validateUrl = (): boolean => {
    let url = this.state.cloudProviderUrl;
    if (url == "") {
      return true;
    } else {
      return url.search(/^[Hh][Tt][Tt][Pp]([Ss]?):\/\//) == 0;
    }
  };

  render() {
    const { open, onCancel, onRegister } = this.props;
    const { cloudProviderUrl } = this.state;
    let isUrlValid = this.validateUrl();
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
              {cloudProviderLangStrings.newcloudprovider.text}
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
              helperText={cloudProviderLangStrings.newcloudprovider.help}
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
