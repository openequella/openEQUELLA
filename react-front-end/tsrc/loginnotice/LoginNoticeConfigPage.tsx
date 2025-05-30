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
import { AxiosError } from "axios";
import * as React from "react";
import {
  generateFromError,
  generateNewErrorID,
  isAxiosError,
} from "../api/errors";
import { withAppContext, AppContextProps } from "../mainui/App";
import SettingPageTemplate from "../components/SettingPageTemplate";
import { routes } from "../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { strings } from "../modules/LoginNoticeModule";
import { languageStrings } from "../util/langstrings";
import PostLoginNoticeConfigurator from "./PostLoginNoticeConfigurator";
import PreLoginNoticeConfigurator from "./PreLoginNoticeConfigurator";

const stringsCommonResults = languageStrings.common.result;

type LoginNoticeConfigPageProps = TemplateUpdateProps & AppContextProps;

interface LoginNoticeConfigPageState {
  notificationOpen: boolean;
  preventNav: boolean;
}

class LoginNoticeConfigPage extends React.Component<
  LoginNoticeConfigPageProps,
  LoginNoticeConfigPageState
> {
  private readonly postLoginNoticeConfigurator: React.RefObject<PostLoginNoticeConfigurator | null>;
  private readonly preLoginNoticeConfigurator: React.RefObject<PreLoginNoticeConfigurator | null>;

  constructor(props: LoginNoticeConfigPageProps) {
    super(props);
    this.preLoginNoticeConfigurator =
      React.createRef<PreLoginNoticeConfigurator>();
    this.postLoginNoticeConfigurator =
      React.createRef<PostLoginNoticeConfigurator>();
  }

  state: LoginNoticeConfigPageState = {
    notificationOpen: false,
    preventNav: false,
  };

  componentDidMount() {
    const { updateTemplate } = this.props;
    updateTemplate((tp) => ({
      ...templateDefaults(strings.title)(tp),
      backRoute: routes.Settings.to,
    }));
  }

  handleError = (error: AxiosError | Error) => {
    let errResponse;
    if (isAxiosError(error) && error.response !== undefined) {
      switch (error.response.status) {
        case 400:
          errResponse = generateNewErrorID(strings.scheduling.endbeforestart);
          break;
        case 403:
          errResponse = generateNewErrorID(strings.errors.permissions);
          break;
        case 404:
          //do nothing, this simply means that there is no current login notice
          return;
      }
    } else {
      errResponse = generateFromError(error);
    }
    if (errResponse) {
      this.props.appErrorHandler(
        errResponse.error_description ?? errResponse.error,
      );
    }
  };

  clearNotifications = () => {
    this.setState({ notificationOpen: false });
  };

  save = async () => {
    try {
      await this.preLoginNoticeConfigurator.current?.save();
      await this.postLoginNoticeConfigurator.current?.save();
      this.setState({ notificationOpen: true });
      this.preventNav(false);
    } catch (error) {
      this.handleError(
        isAxiosError(error) || error instanceof Error
          ? error
          : new Error(stringsCommonResults.fail),
      );
    }
  };

  preventNav = (preventNav: boolean) => {
    this.setState({ preventNav });
  };

  render() {
    return (
      <SettingPageTemplate
        onSave={() => this.save()}
        snackbarOpen={this.state.notificationOpen}
        snackBarOnClose={() => this.setState({ notificationOpen: false })}
        saveButtonDisabled={!this.state.preventNav}
        preventNavigation={this.state.preventNav}
      >
        <PreLoginNoticeConfigurator
          handleError={this.handleError}
          ref={this.preLoginNoticeConfigurator}
          preventNav={this.preventNav}
        />
        <PostLoginNoticeConfigurator
          handleError={this.handleError}
          ref={this.postLoginNoticeConfigurator}
          preventNav={this.preventNav}
        />
      </SettingPageTemplate>
    );
  }
}

export default withAppContext(LoginNoticeConfigPage);
