import axios, {AxiosPromise} from "axios";
import {Config} from "../config";

export const apiURL = `${Config.baseUrl}api/loginnotice`;

export function submitNotice(notice:string):AxiosPromise{
  return axios.put(apiURL, notice);
}

export function getNotice():AxiosPromise{
  return axios.get(apiURL);
}

export function deleteNotice():AxiosPromise{
  return axios.delete(apiURL);
}
