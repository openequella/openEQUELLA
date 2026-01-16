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
import { useContext, useEffect } from "react";
import { AppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import WelcomeBoard from "./components/WelcomeBoard";
import { DashboardPage } from "./DashboardPage";

const { title } = languageStrings.dashboard;

/**
 * Top-level Dashboard component that determines whether to show the system user
 * welcome board or the regular dashboard page based on the current user type.
 */
const Dashboard: React.FC<TemplateUpdateProps> = ({ updateTemplate }) => {
  const { currentUser } = useContext(AppContext);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(title)(tp),
    }));
  }, [updateTemplate]);

  return currentUser?.isSystem ? (
    <WelcomeBoard isSystemUser />
  ) : (
    <DashboardPage />
  );
};

export default Dashboard;
