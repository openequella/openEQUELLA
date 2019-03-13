import { createStyles, Theme } from "@material-ui/core";
import { PagingResults } from "../api";
import Axios from "axios";
import { Config } from "../config";
import { CloudProviderEntity } from "./CloudProviderEntity";
import { prepLangStrings } from "../util/langstrings";

export const GET_CLOUD_PROVIDER_URL = `${Config.baseUrl}api/cloudprovider`;

export const langStrings = prepLangStrings("cp", {
  title: "Cloud Provider",
  cloudProviderAvailable: {
    zero: "No cloud providers available",
    one: "%d cloud provider",
    more: "%d cloud providers"
  }
});

export function getCloudProviderListPageStyle() {
  const styles = (theme: Theme) =>
    createStyles({
      fab: {
        zIndex: 1000,
        position: "fixed",
        bottom: theme.spacing.unit * 2,
        right: theme.spacing.unit * 5
      }
    });
  return styles;
}

export function getCloudProviderListStyle() {
  const styles = (theme: Theme) =>
    createStyles({
      overall: {
        padding: theme.spacing.unit * 2,
        height: "100%"
      },
      results: {
        padding: theme.spacing.unit * 2,
        position: "relative"
      },
      resultHeader: {
        display: "flex",
        justifyContent: "flex-end"
      },
      resultText: {
        flexGrow: 1
      },
      searchResultContent: {
        marginTop: theme.spacing.unit
      }
    });
  return styles;
}

export function getCloudProviders(): Promise<
  PagingResults<CloudProviderEntity>
> {
  return Axios.get<PagingResults<CloudProviderEntity>>(
    GET_CLOUD_PROVIDER_URL
  ).then(res => res.data);
}
