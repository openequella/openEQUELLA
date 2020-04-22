import * as React from "react";
import { ChangeEvent } from "react";
import {
  FormControl,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  Typography,
} from "@material-ui/core";
import {
  clearPreLoginNotice,
  getPreLoginNotice,
  NotificationType,
  PreLoginNotice,
  ScheduleTypeSelection,
  strings,
  submitPreLoginNotice,
  unMarshallPreLoginNotice,
  uploadPreLoginNoticeImage,
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import RichTextEditor from "../components/RichTextEditor";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import { DateTimePicker } from "material-ui-pickers";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
  preventNav: (prevNav: boolean) => void;
}

interface PreLoginNoticeConfiguratorState {
  current: PreLoginNotice;
  db: PreLoginNotice;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      current: {
        notice: "",
        scheduleSettings: ScheduleTypeSelection.ON,
        startDate: new Date(),
        endDate: new Date(),
      },
      db: {
        notice: "",
        scheduleSettings: ScheduleTypeSelection.ON,
        startDate: new Date(),
        endDate: new Date(),
      },
    };
  }

  handleSubmitPreNotice = () => {
    if (this.state.current.notice == "") {
      clearPreLoginNotice()
        .then(() => {
          this.props.notify(NotificationType.Clear);
          this.setState(
            {
              db: this.state.current,
            },
            () => this.setPreventNav()
          );
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    } else {
      submitPreLoginNotice(this.state.current)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setDBToValues();
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  setDBToValues = () => {
    this.setState(
      {
        db: this.state.current,
      },
      () => this.setPreventNav()
    );
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse<PreLoginNotice>) => {
        const preLoginNotice: PreLoginNotice = unMarshallPreLoginNotice(
          response.data
        );
        if (preLoginNotice.notice != undefined) {
          this.setState({
            db: preLoginNotice,
            current: preLoginNotice,
          });
        }
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  setPreventNav = () => {
    this.props.preventNav(
      this.state.db.scheduleSettings != this.state.current.scheduleSettings ||
        this.state.db.endDate != this.state.current.endDate ||
        this.state.db.startDate != this.state.current.startDate ||
        this.state.db.notice != this.state.current.notice
    );
  };

  handleEditorChange = (html: string) => {
    this.setState(
      {
        current: { ...this.state.current, notice: html },
      },
      () => this.setPreventNav()
    );
  };

  isNoticeCurrent = (): boolean => {
    return this.state.current.endDate.getTime() > new Date().getTime();
  };

  ScheduleSettings = () => {
    return (
      <FormControl>
        <Typography color="textSecondary" variant="subtitle1">
          {strings.scheduling.title}
        </Typography>
        <RadioGroup
          row
          value={ScheduleTypeSelection[this.state.current.scheduleSettings]}
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
          hidden={
            this.state.current.scheduleSettings !=
            ScheduleTypeSelection.SCHEDULED
          }
        >
          <div hidden={this.isNoticeCurrent()}>
            <Typography color="error" variant="subtitle1">
              {strings.scheduling.expired}
            </Typography>
          </div>
          <Typography color="textSecondary" variant="subtitle1">
            {strings.scheduling.start}
          </Typography>
          <DateTimePicker
            id="startDatePicker"
            okLabel={<span id="ok">OK</span>}
            onChange={this.handleStartDateChange}
            format={"d MMM yyyy - h:mm a"}
            value={this.state.current.startDate}
          />

          <Typography color="textSecondary" variant="subtitle1">
            {strings.scheduling.end}
          </Typography>

          <DateTimePicker
            id="endDatePicker"
            onChange={this.handleEndDateChange}
            format={"d MMM yyyy - h:mm a"}
            value={this.state.current.endDate}
          />
        </div>
      </FormControl>
    );
  };

  handleStartDateChange = (startDate: Date) => {
    this.setState(
      { current: { ...this.state.current, startDate: new Date(startDate) } },
      () => this.setPreventNav()
    );
  };

  handleEndDateChange = (endDate: Date) => {
    this.setState(
      { current: { ...this.state.current, endDate: new Date(endDate) } },
      () => this.setPreventNav()
    );
  };

  handleScheduleTypeSelectionChange = (
    event: ChangeEvent<{}>,
    value: string
  ) => {
    this.setState(
      {
        current: {
          ...this.state.current,
          scheduleSettings:
            ScheduleTypeSelection[value as ScheduleTypeSelection],
        },
      },
      () => this.setPreventNav()
    );
  };

  render() {
    const ScheduleSettings = this.ScheduleSettings;
    return (
      <SettingsMenuContainer>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <RichTextEditor
              htmlInput={this.state.db.notice}
              onStateChange={this.handleEditorChange}
              imageUploadCallBack={uploadPreLoginNoticeImage}
            />
          </Grid>
          <Grid item>
            <ScheduleSettings />
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
