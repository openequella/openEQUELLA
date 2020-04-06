import { templateError, TemplateUpdate } from "../../../mainui/Template";
import Axios from "axios";
import { fromAxiosError } from "../../../api/errors";

export interface MIMETypeFilter {
  id: string;
  name: string;
  mimeTypes: string[];
}

const MIME_TYPE_FILTERS_URL = "api/settings/search/filter";

export const getMIMETypeFiltersFromServer = (): Promise<MIMETypeFilter[]> =>
  Axios.get(MIME_TYPE_FILTERS_URL)
    .then(response => response.data)
    .catch(error => templateError(fromAxiosError(error)));

export const deleteMIMETypeFilterFromServer = (uuid: string) => {
  return new Promise(
    (
      resolve: (code: number) => void,
      reject: (error: TemplateUpdate) => void
    ) => {
      Axios.delete(`${MIME_TYPE_FILTERS_URL}/${uuid}`).catch(error =>
        reject(templateError(fromAxiosError(error)))
      );
    }
  );
};
