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
import CloudIcon from "@mui/icons-material/CloudCircleRounded";
import DeleteIcon from "@mui/icons-material/Delete";
import { Avatar, IconButton } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { sprintf } from "sprintf-js";
import ConfirmDialog from "../components/ConfirmDialog";
import EntityList from "../components/EntityList";
import EquellaListItem from "../components/EquellaListItem";
import MessageInfo from "../components/MessageInfo";
import { AppContextProps, withAppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { commonString } from "../util/commonstrings";
import { formatSize } from "../util/langstrings";
import CloudProviderAddDialog from "./CloudProviderAddDialog";
import { CloudProviderEntity } from "./CloudProviderEntity";
import {
  cloudProviderLangStrings,
  deleteCloudProvider,
  getCloudProviders,
  refreshCloudProvider,
  registerCloudProviderInit,
} from "./CloudProviderModule";

const StyledCloudIcon = styled(CloudIcon)({
  width: 40,
  height: 40,
});

type CloudProviderBasicProps = TemplateUpdateProps;

type CloudProviderListPageProps = CloudProviderBasicProps & AppContextProps;

interface CloudProviderListPageState {
  cloudProviders: CloudProviderEntity[];
  deleteDialogOpen: boolean;
  registerDialogOpen: boolean;
  deleteDetails?: CloudProviderEntity;
  showRefreshed: boolean;
}

class CloudProviderListPage extends React.Component<
  CloudProviderListPageProps,
  CloudProviderListPageState
> {
  constructor(props: CloudProviderListPageProps) {
    super(props);
    this.state = {
      cloudProviders: [],
      deleteDialogOpen: false,
      registerDialogOpen: false,
      showRefreshed: false,
    };
  }

  handleError = this.props.appErrorHandler;

  componentDidMount(): void {
    this.getCloudProviderList();
    this.props.updateTemplate(templateDefaults(cloudProviderLangStrings.title));
  }

  getCloudProviderList = () => {
    getCloudProviders()
      .then((result) => {
        this.setState((_) => ({
          cloudProviders: result.results,
        }));
      })
      .catch(this.handleError);
  };

  deleteCloudProvider = (cloudProvider: CloudProviderEntity) => {
    this.setState({
      deleteDialogOpen: true,
      deleteDetails: cloudProvider,
    });
  };

  refreshProvider = (cloudProvider: CloudProviderEntity) => {
    refreshCloudProvider(cloudProvider.id).then((_) => {
      this.getCloudProviderList();
      this.setState({ showRefreshed: true });
    });
  };

  cancelDeleteCloudProvider = () => {
    this.setState({
      deleteDialogOpen: false,
    });
  };

  confirmDeleteCloudProvider = () => {
    if (this.state.deleteDetails) {
      const id = this.state.deleteDetails.id;
      this.cancelDeleteCloudProvider();
      deleteCloudProvider(id)
        .then(() => {
          this.getCloudProviderList();
        })
        .catch(this.handleError);
    }
  };

  registerCloudProvider = () => {
    this.setState({
      registerDialogOpen: true,
    });
  };

  cancelRegisterCloudProvider = () => {
    this.setState({
      registerDialogOpen: false,
    });
  };

  confirmRegisterCloudProvider = (url: string) => {
    registerCloudProviderInit(url)
      .then((result) => {
        window.location.href = result.url;
      })
      .catch(this.handleError);
  };

  render() {
    const { cloudProviders, deleteDialogOpen, registerDialogOpen } = this.state;
    return (
      <>
        <MessageInfo
          variant="success"
          open={this.state.showRefreshed}
          title={cloudProviderLangStrings.refreshed}
          onClose={() => this.setState({ showRefreshed: false })}
        />
        {this.state.deleteDetails && (
          <ConfirmDialog
            open={deleteDialogOpen}
            title={sprintf(
              cloudProviderLangStrings.deletecloudprovider.title,
              this.state.deleteDetails.name,
            )}
            onConfirm={this.confirmDeleteCloudProvider}
            onCancel={this.cancelDeleteCloudProvider}
            confirmButtonText={commonString.action.delete}
          >
            {cloudProviderLangStrings.deletecloudprovider.message}
          </ConfirmDialog>
        )}
        <CloudProviderAddDialog
          open={registerDialogOpen}
          onCancel={this.cancelRegisterCloudProvider}
          onRegister={this.confirmRegisterCloudProvider}
        />
        <EntityList
          id="cloudProviderList"
          resultsText={formatSize(
            cloudProviders.length,
            cloudProviderLangStrings.cloudprovideravailable,
          )}
          progress={false}
          createOnClick={this.registerCloudProvider}
        >
          {cloudProviders.map((cloudProvider) => {
            const secondaryAction = (
              <IconButton
                onClick={() => {
                  this.deleteCloudProvider(cloudProvider);
                }}
                size="large"
              >
                <DeleteIcon />
              </IconButton>
            );
            const icon = (
              <Avatar
                src={cloudProvider.iconUrl}
                alt={cloudProvider.description}
              >
                {!cloudProvider.iconUrl && <StyledCloudIcon />}
              </Avatar>
            );

            const secondaryText = cloudProvider.canRefresh ? (
              <>
                {cloudProvider.description} -{" "}
                <a
                  href="/#"
                  onClick={(event: React.MouseEvent<HTMLAnchorElement>) => {
                    event.preventDefault();
                    this.refreshProvider(cloudProvider);
                  }}
                >
                  {commonString.action.refresh}
                </a>
              </>
            ) : (
              cloudProvider.description
            );
            return (
              <EquellaListItem
                key={cloudProvider.id}
                listItemPrimaryText={cloudProvider.name}
                listItemSecondText={secondaryText}
                listItemAttributes={{ divider: true }}
                icon={icon}
                secondaryAction={secondaryAction}
              />
            );
          })}
        </EntityList>
      </>
    );
  }
}

export default withAppContext(CloudProviderListPage);
