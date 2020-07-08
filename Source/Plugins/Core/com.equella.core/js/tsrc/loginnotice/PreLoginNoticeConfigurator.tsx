/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from "react";
import { ChangeEvent } from "react";
import {
  Card,
  CardContent,
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
  PreLoginNotice,
  ScheduleTypeSelection,
  strings,
  submitPreLoginNotice,
  unMarshallPreLoginNotice,
  uploadPreLoginNoticeImage,
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import { DateTimePicker } from "material-ui-pickers";
import SettingsListHeading from "../components/SettingsListHeading";

const RichTextEditor = React.lazy(() => import("../components/RichTextEditor"));

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
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

  save = async () =>
    (this.state.current.notice
      ? submitPreLoginNotice(this.state.current)
      : clearPreLoginNotice()
    ).then(() => this.setState({ db: this.state.current }));

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
      <Card>
        <CardContent>
          <SettingsListHeading heading={strings.preLogin.title} />
          <Grid id="preLoginConfig" container spacing={2} direction="column">
            <Grid item>
              <React.Suspense fallback={<div>Loading editor...</div>}>
                <RichTextEditor
                  htmlInput={this.state.db.notice}
                  onStateChange={this.handleEditorChange}
                  imageUploadCallBack={uploadPreLoginNoticeImage}
                />
              </React.Suspense>
            </Grid>
            <Grid item>
              <ScheduleSettings />
            </Grid>
          </Grid>
        </CardContent>
      </Card>
    );
  }
}

export default PreLoginNoticeConfigurator;
