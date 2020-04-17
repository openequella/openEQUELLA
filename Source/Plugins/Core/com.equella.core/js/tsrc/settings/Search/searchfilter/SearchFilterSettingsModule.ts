import Axios from "axios";
import { encodeQuery } from "../../../util/encodequery";
import {
  BatchOperationResponse,
  groupErrorMessages
} from "../../../api/BatchOperationResponse";

export interface MimeTypeFilter {
  id?: string;
  name: string;
  mimeTypes: string[];
}

export interface MimeTypeEntry {
  mimeType: string;
  desc: string;
}

const MIME_TYPE_FILTERS_URL = "api/settings/search/filter";
const MIME_TYPE_URL = "api/settings/mimetype";

export const getMimeTypeFiltersFromServer = (): Promise<MimeTypeFilter[]> =>
  Axios.get(MIME_TYPE_FILTERS_URL).then(res => res.data);

export const getMIMETypesFromServer = (): Promise<MimeTypeEntry[]> =>
  Axios.get(MIME_TYPE_URL).then(res => res.data);

export const batchUpdateOrAdd = (filters: MimeTypeFilter[]) =>
  Axios.put<BatchOperationResponse[]>(
    MIME_TYPE_FILTERS_URL,
    filters
  ).then(res => groupErrorMessages(res.data));

export const batchDelete = (ids: string[]) =>
  Axios.delete<BatchOperationResponse[]>(
    `${MIME_TYPE_FILTERS_URL}/${encodeQuery({ ids: ids })}`
  ).then(res => groupErrorMessages(res.data));

export const getMimeTypeDetail = (entry: MimeTypeEntry) => {
  const { mimeType, desc } = entry;
  if (desc) {
    return `${desc} (${mimeType})`;
  }
  return mimeType;
};

export const vaidateMimeTypeName = (name: string | undefined): boolean =>
  !!name?.trim();
