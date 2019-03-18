import { PagingResults } from "../api";
import Axios from "axios";
import { Config } from "../config";
import { CloudProviderEntity } from "./CloudProviderEntity";
import { prepLangStrings } from "../util/langstrings";

export const GET_CLOUD_PROVIDER_URL = `${Config.baseUrl}api/cloudprovider`;

export const langStrings = prepLangStrings("cp", {
  title: "cloud providers",
  cloudProviderAvailable: {
    zero: "No cloud providers available",
    one: "%d cloud provider",
    more: "%d cloud providers"
  }
});

export function getCloudProviders(): Promise<
  PagingResults<CloudProviderEntity>
> {
  return Axios.get<PagingResults<CloudProviderEntity>>(
    GET_CLOUD_PROVIDER_URL
  ).then(res => res.data);
}
