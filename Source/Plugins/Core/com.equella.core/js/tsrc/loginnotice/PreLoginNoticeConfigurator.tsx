import * as React from "react";
import {
  Button,
  FormControl,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  Typography
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";
import {
  clearPreLoginNotice,
  emptyTinyMCEString,
  getPreLoginNotice,
  NotificationType,
  PreLoginNotice,
  ScheduleTypeSelection,
  strings,
  submitPreLoginNotice,
  uploadPreLoginNoticeImage
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import RichTextEditor from "../components/RichTextEditor";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import { DatePicker } from "material-ui-pickers";
import { ChangeEvent } from "react";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
}

interface PreLoginNoticeConfiguratorState {
  html: string;
  dbHtml: string;
  scheduleType: ScheduleTypeSelection;
  dbScheduleType: ScheduleTypeSelection;
  startDate?: Date;
  endDate?: Date;
  dbStartDate?: Date;
  dbEndDate?: Date;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      html: "",
      dbHtml: "",
      scheduleType: ScheduleTypeSelection.ON,
      dbScheduleType: ScheduleTypeSelection.ON,
      startDate: new Date(),
      dbStartDate: new Date(),
      endDate: new Date(),
      dbEndDate: new Date()
    };
  }

  handleSubmitPreNotice = () => {
    if (this.state.html == emptyTinyMCEString) {
      clearPreLoginNotice()
        .then(() => {
          this.props.notify(NotificationType.Clear);
          this.setState({
            dbHtml: this.state.html
          });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    } else {
      let noticeToSend: PreLoginNotice = {
        notice: this.state.html,
        scheduleSettings: this.state.scheduleType,
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

  setValuesToDB = () => {
    this.setState({
      html: this.state.dbHtml,
      scheduleType: this.state.dbScheduleType,
      startDate: this.state.dbStartDate,
      endDate: this.state.dbEndDate
    });
  };

  setDBToValues = () => {
    this.setState({
      dbHtml: this.state.html,
      dbScheduleType: this.state.scheduleType,
      dbStartDate: this.state.startDate,
      dbEndDate: this.state.endDate
    });
  };

  handleUndoPreNotice = () => {
    this.setState(
      {
        //swap the states to force an update
        html: this.state.dbHtml,
        dbHtml: this.state.html
      },
      () => this.setState({ dbHtml: this.state.html })
    ); //set the dbHtml back to it's original value to update the editor
    this.props.notify(NotificationType.Revert);
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse) => {
        if (response.data.notice != undefined) {
          this.setState({
            dbHtml: response.data.notice,
            dbScheduleType: response.data.scheduleSettings,
            dbStartDate: response.data.startDate,
            dbEndDate: response.data.endDate
          });
          this.setValuesToDB();
        }
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleEditorChange = (html: string) => {
    this.setState({ html });
  };

  areButtonsEnabled = (): boolean => {
    //state matches database?
    return (
      this.state.scheduleType == this.state.dbScheduleType &&
      this.state.html == this.state.dbHtml &&
      this.state.startDate == this.state.dbStartDate &&
      this.state.endDate == this.state.dbEndDate
    );
  };

  ScheduleSettings = () => {
    return (
      <FormControl>
        <Typography color="textSecondary" variant="subheading">
          {strings.scheduling.title}
        </Typography>

        <RadioGroup
          row
          value={ScheduleTypeSelection[this.state.scheduleType]}
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
            value={ScheduleTypeSelection[ScheduleTypeSelection.OFF]}
            label={strings.scheduling.disabled}
            control={<Radio id="offRadioButton" />}
          />
        </RadioGroup>

        <div
          hidden={this.state.scheduleType != ScheduleTypeSelection.SCHEDULED}
        >
          <Typography color="textSecondary" variant="subheading">
            {strings.scheduling.start}
          </Typography>

          <DatePicker
            id="startDatePicker"
            okLabel={<span id="ok">OK</span>}
            format={"DDD"}
            minDate={new Date().toLocaleDateString()}
            onChange={this.handleStartDateChange}
            value={this.state.startDate}
          />

          <Typography color="textSecondary" variant="subheading">
            {strings.scheduling.end}
          </Typography>

          <DatePicker
            id="endDatePicker"
            minDate={this.state.startDate}
            format={"DDD"}
            minDateMessage={strings.scheduling.endbeforestart}
            onChange={this.handleEndDateChange}
            value={this.state.endDate}
          />
        </div>
      </FormControl>
    );
  };

  handleStartDateChange = (startDate: Date) => {
    this.setState({ startDate });
  };

  handleEndDateChange = (endDate: Date) => {
    this.setState({ endDate });
  };

  handleScheduleTypeSelectionChange = (event: ChangeEvent, value: string) => {
    this.setState({ scheduleType: ScheduleTypeSelection[value] });
  };

  render() {
    const ScheduleSettings = this.ScheduleSettings;
    return (
      <SettingsMenuContainer>
        <Typography color="textSecondary" variant="subtitle1">
          {strings.prelogin.label}
        </Typography>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <RichTextEditor
              htmlInput={this.state.dbHtml}
              onStateChange={this.handleEditorChange}
              imageUploadCallBack={uploadPreLoginNoticeImage}
            />
          </Grid>
          <Grid item>
            <ScheduleSettings />
          </Grid>
          <Grid item container spacing={8} direction="row-reverse">
            <Grid item>
              <Button
                id="preApplyButton"
                onClick={this.handleSubmitPreNotice}
                variant="contained"
                disabled={this.areButtonsEnabled()}
              >
                {commonString.action.save}
              </Button>
            </Grid>
            <Grid item>
              <Button
                id="preUndoButton"
                onClick={this.handleUndoPreNotice}
                variant="text"
                disabled={this.areButtonsEnabled()}
              >
                {commonString.action.cancel}
              </Button>
            </Grid>
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
