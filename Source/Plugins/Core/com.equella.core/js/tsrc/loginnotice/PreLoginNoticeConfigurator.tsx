import * as React from "react";
import { ChangeEvent } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  TextField,
  Typography
} from "@material-ui/core";
import { DatePicker } from "material-ui-pickers";

import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  PreLoginNotice,
  ScheduleTypeSelection,
  strings,
  submitPreLoginNotice
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import SettingsMenuContainer from "../components/SettingsMenuContainer";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PreLoginNoticeConfiguratorState {
  preNotice?: string; //what is currently in the textfield
  dbPreNotice?: string; //what is currently in the database
  clearStaged: boolean;
  checked: ScheduleTypeSelection;
  dbChecked: ScheduleTypeSelection;
  startDate?: Date;
  dbStartDate?: Date;
  endDate?: Date;
  dbEndDate?: Date;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      preNotice: "",
      dbPreNotice: "",
      clearStaged: false,
      checked: ScheduleTypeSelection.ON,
      dbChecked: ScheduleTypeSelection.ON,
      startDate: new Date(),
      dbStartDate: new Date(),
      endDate: new Date(),
      dbEndDate: new Date()
    };
  }

  handleSubmitPreNotice = () => {
    if (this.state.preNotice != undefined) {
      let noticeToSend: PreLoginNotice = {
        notice: this.state.preNotice,
        scheduleSettings: this.state.checked,
        startDate: this.state.startDate,
        endDate: this.state.endDate
      };
      submitPreLoginNotice(noticeToSend)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setDBToValues();
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  handleClearPreNotice = () => {
    this.setState({ preNotice: "" });
    clearPreLoginNotice()
      .then(() => {
        this.setState({ dbPreNotice: "", clearStaged: false });
        this.props.notify(NotificationType.Clear);
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleUndoPreNotice = () => {
    this.setValuesToDB();
    this.props.notify(NotificationType.Revert);
  };

  handlePreTextFieldChange = (
    e: HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
  ) => {
    this.setState({ preNotice: e.value });
  };

  setValuesToDB = () => {
    this.setState({
      preNotice: this.state.dbPreNotice,
      checked: this.state.dbChecked,
      startDate: this.state.dbStartDate,
      endDate: this.state.dbEndDate
    });
  };

  setDBToValues = () => {
    this.setState({
      dbPreNotice: this.state.preNotice,
      dbChecked: this.state.checked,
      dbStartDate: this.state.startDate,
      dbEndDate: this.state.endDate
    });
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse<PreLoginNotice>) => {
        this.setState({
          dbPreNotice: response.data.notice,
          dbChecked: response.data.scheduleSettings,
          dbStartDate: response.data.startDate,
          dbEndDate: response.data.endDate
        });
        this.setValuesToDB();
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  stageClear = () => {
    this.setState({ clearStaged: true });
  };

  Dialogs = () => {
    return (
      <div>
        <Dialog
          open={this.state.clearStaged}
          onClose={() => this.setState({ clearStaged: false })}
        >
          <DialogTitle>{strings.clear.title}</DialogTitle>
          <DialogContent>
            <DialogContentText>{strings.clear.confirm}</DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button id="okToClear" onClick={this.handleClearPreNotice}>
              {commonString.action.ok}
            </Button>
            <Button
              id="cancelClear"
              onClick={() => this.setState({ clearStaged: false })}
            >
              {commonString.action.cancel}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  };

  areButtonsEnabled = (): boolean => {
    //state matches database?
    return (
      this.state.checked == this.state.dbChecked &&
      this.state.preNotice == this.state.dbPreNotice &&
      this.state.startDate == this.state.dbStartDate &&
      this.state.endDate == this.state.dbEndDate
    );
  };

  handleScheduleTypeSelectionChange = (event: ChangeEvent, value: string) => {
    this.setState({ checked: ScheduleTypeSelection[value] });
  };

  handleStartDateChange = (value: Date) => {
    this.setState({ startDate: value });
  };

  handleEndDateChange = (value: Date) => {
    this.setState({ endDate: value });
  };

  resetDatePickers = () => {
    this.setState({ startDate: undefined, endDate: undefined });
  };

  ScheduleSettings = () => {
    return (
      <FormControl>
        <Typography color="textSecondary" variant="subheading">
          {strings.scheduling.title}
        </Typography>

        <RadioGroup
          row
          value={ScheduleTypeSelection[this.state.checked]}
          onChange={this.handleScheduleTypeSelectionChange}
        >
          <FormControlLabel
            value={ScheduleTypeSelection[ScheduleTypeSelection.ON]}
            label={strings.scheduling.alwayson}
            control={<Radio id="onRadioButton" />}
          />
          <FormControlLabel
            value={ScheduleTypeSelection[ScheduleTypeSelection.SCHEDULED]}
            label={strings.scheduling.scheduled}
            control={<Radio id="scheduledRadioButton" />}
          />
          <FormControlLabel
            value="OFF"
            label={strings.scheduling.disabled}
            control={<Radio id="offRadioButton" />}
          />
        </RadioGroup>

        <div hidden={this.state.checked != ScheduleTypeSelection.SCHEDULED}>
          <Typography color="textSecondary" variant="subheading">
            {strings.scheduling.start}
          </Typography>

          <DatePicker
            id="startDatePicker"
            okLabel={<span id="ok">OK</span>}
            minDate={new Date().toLocaleDateString()}
            onChange={this.handleStartDateChange}
            value={
              this.state.startDate == undefined ? null : this.state.startDate
            }
          />

          <Typography color="textSecondary" variant="subheading">
            {strings.scheduling.end}
          </Typography>

          <DatePicker
            id="endDatePicker"
            minDate={this.state.startDate}
            minDateMessage={strings.scheduling.endbeforestart}
            onChange={this.handleEndDateChange}
            value={this.state.endDate == undefined ? null : this.state.endDate}
          />
        </div>
      </FormControl>
    );
  };

  render() {
    const { preNotice, dbPreNotice } = this.state;
    const Dialogs = this.Dialogs;
    const ScheduleSettings = this.ScheduleSettings;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subheading">
          {strings.prelogin.label}
        </Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <TextField
              id="preNoticeField"
              variant="outlined"
              rows="12"
              rowsMax="35"
              multiline
              fullWidth
              inputProps={{ length: 12 }}
              placeholder={strings.prelogin.description}
              onChange={e => this.handlePreTextFieldChange(e.target)}
              value={preNotice}
            />
          </Grid>
          <Grid item>
            <ScheduleSettings />
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button
                id="preApplyButton"
                disabled={this.areButtonsEnabled()}
                onClick={this.handleSubmitPreNotice}
                variant="contained"
              >
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preClearButton"
                disabled={dbPreNotice == ""}
                onClick={this.stageClear}
                variant="text"
              >
                {commonString.action.clear}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
                disabled={this.areButtonsEnabled()}
                onClick={this.handleUndoPreNotice}
                variant="text"
              >
                {commonString.action.revertchanges}
              </Button>
            </Grid>
          </Grid>
        </Grid>
        <Dialogs />
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
