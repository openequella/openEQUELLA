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
import { absurd } from "fp-ts/function";
import { hasConfigurationChanged, defaultConfig } from "./OidcSettingsHelper";
import * as OEQ from "@openequella/rest-api-client";

export type Action =
  | {
      type: "init-complete";
      initialConfig?: OEQ.Oidc.IdentityProvider;
      serverHasConfig: boolean;
    }
  | {
      type: "configure";
      config: OEQ.Oidc.IdentityProvider;
    }
  | {
      type: "submit";
    }
  | {
      type: "reset";
    };

interface CommonState {
  /**
   * Initial configuration retrieved from server, but before the config is returned,
   * use the defaults values of each state listed above.
   */
  initialConfig: OEQ.Oidc.IdentityProvider;
  /**
   * Whether the server has configuration.
   */
  serverHasConfig: boolean;
  /**
   * The values of the OIDC configuration.
   */
  config: OEQ.Oidc.IdentityProvider;
}

export type State =
  | ({
      status: "initialising";
      isConfigChanged: false;
    } & CommonState)
  | ({
      status: "configuring";
      // Whether the configuration has changed by user.
      isConfigChanged: boolean;
    } & CommonState)
  | ({
      status: "saving";
      isConfigChanged: true;
    } & CommonState);

/**
 * Because Entra ID is selected by default, use the default Entra ID API values for apiDetails.
 */
export const initialState: State = {
  status: "initialising",
  initialConfig: defaultConfig,
  serverHasConfig: false,
  config: defaultConfig,
  isConfigChanged: false,
};

/**
 * It handles 4 actions:
 *
 * 1. init-complete:
 * this is when it gets the initial configuration from the server, and overrides the default values.
 * Also, it will change the page status to "configuring".
 *
 * 2. configure:
 * this is when the user changes the configuration, and it updates the related OIDC configurations accordingly.
 *
 * 3. submit:
 * this is when the user submits the configuration, and it sets the status to "saving".
 *
 * 4. reset:
 * this is when the user successfully save the configuration, it will reset the initialConfig value
 * and it sets the status to "configuring".
 */
export const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "init-complete":
      return {
        status: "configuring",
        // If the initialConfig is not provided, keep use the previous(default) values.
        initialConfig: action.initialConfig ?? state.initialConfig,
        serverHasConfig: action.serverHasConfig,
        isConfigChanged: false,
        // If the initialConfig is not provided, keep use the previous(default) values.
        config: action.initialConfig ?? state.config,
      };
    case "configure":
      return {
        ...state,
        status: "configuring",
        isConfigChanged: hasConfigurationChanged(
          state.initialConfig,
          action.config,
        ),
        config: action.config,
      };
    case "submit":
      return {
        ...state,
        status: "saving",
        isConfigChanged: true,
      };
    case "reset":
      return {
        ...state,
        status: "configuring",
        initialConfig: state.config,
        isConfigChanged: false,
        serverHasConfig: true,
      };
    default:
      return absurd(action);
  }
};
