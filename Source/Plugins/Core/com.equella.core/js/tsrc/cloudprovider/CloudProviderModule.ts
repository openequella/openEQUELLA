import { PagingResults } from "../api";
import Axios, { AxiosPromise } from "axios";
import { Config } from "../config";
import { CloudProviderEntity } from "./CloudProviderEntity";
import { prepLangStrings } from "../util/langstrings";

export const GET_CLOUD_PROVIDER_LIST_URL = `${Config.baseUrl}api/cloudprovider`;
export const DELETE_CLOUD_PROVIDER_URL = `${
  Config.baseUrl
}api/cloudprovider/provider`;
export const POST_CLOUD_PROVIDER_REGISTER_INIT_URL = `${
  Config.baseUrl
}api/cloudprovider/register/init`;
export const cloudProviderLangStrings = prepLangStrings("cp", {
  title: "Cloud providers",
  cloudprovideravailable: {
    zero: "No cloud providers available",
    one: "%d cloud provider",
    more: "%d cloud providers"
  },
  newcloudprovider: {
    title: "Register a new cloud provider",
    label: "URL",
    help: "Enter the URL supplied by the cloud provider",
    error: "The URL should start with either http:// or https://"
  },
  deletecloudprovider: {
    title: "Are you sure you want to delete cloud provider - '%s'?",
    message: "It will be permanently deleted."
  }
});
interface CloudProviderInitResponse {
  url: string;
}

export function getCloudProviders(): Promise<
  PagingResults<CloudProviderEntity>
> {
  return Axios.get<PagingResults<CloudProviderEntity>>(
    GET_CLOUD_PROVIDER_LIST_URL
  ).then(res => res.data);
}

export function deleteCloudProvider(cloudProviderId: string): AxiosPromise {
  return Axios.delete(DELETE_CLOUD_PROVIDER_URL + "/" + cloudProviderId);
}

export function registerCloudProviderInit(
  cloudProviderUrl: string
): Promise<CloudProviderInitResponse> {
  let params = {
    url: cloudProviderUrl
  };
  return Axios.post<CloudProviderInitResponse>(
    POST_CLOUD_PROVIDER_REGISTER_INIT_URL,
    null,
    {
      params: params
    }
  ).then(res => res.data);
}
