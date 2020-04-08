import Axios from "axios";

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

export const getMimeTypeDetail = (entry: MimeTypeEntry) => {
  const { mimeType, desc } = entry;
  if (desc) {
    return `${desc} (${mimeType})`;
  }
  return mimeType;
};

export const batchUpdateOrAdd = (filters: MimeTypeFilter[]) => {
  return Axios.put(MIME_TYPE_FILTERS_URL, filters);
};

export const vaidateMimeTypeName = (name: string | undefined): boolean => {
  if (name?.trim()) {
    return true;
  }
  return false;
};
