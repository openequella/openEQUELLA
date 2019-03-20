import { PagingResults } from "../api";
import Axios from "axios";
import { Config } from "../config";
import { CloudProviderEntity } from "./CloudProviderEntity";
import { prepLangStrings } from "../util/langstrings";

export const GET_CLOUD_PROVIDER_LIST_URL = `${Config.baseUrl}api/cloudprovider`;
export const POST_CLOUD_PROVIDER_REGISTER_INIT_URL = `${
  Config.baseUrl
}api/cloudprovider/register/init`;
export const langStrings = prepLangStrings("cp", {
  title: "Cloud providers",
  cloudProviderAvailable: {
    zero: "No cloud providers available",
    one: "%d cloud provider",
    more: "%d cloud providers"
  },
  newCloudProviderTitle: "Create cloud providers",
  newCloudProviderInfo: {
    id: "new_cloud_provider_url",
    label: "URL",
    help: "Cloud provider URL, e.g. www.equella.com/upload"
  }
});

export function getCloudProviders(): Promise<
  PagingResults<CloudProviderEntity>
> {
  return Axios.get<PagingResults<CloudProviderEntity>>(
    GET_CLOUD_PROVIDER_LIST_URL
  ).then(res => res.data);
}

export function registerCloudProviderInit(params: object) {
  return Axios.post(POST_CLOUD_PROVIDER_REGISTER_INIT_URL, null, {
    params: params
  });
}
