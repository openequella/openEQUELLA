import { Config } from "../config";
import Axios from "axios";

export interface SearchConfig {
  id: string;
}

export function listAllConfigs(): Promise<SearchConfig[]> {
  return Axios.get(`${Config.baseUrl}api/searches/config`).then(
    res => res.data
  );
}
