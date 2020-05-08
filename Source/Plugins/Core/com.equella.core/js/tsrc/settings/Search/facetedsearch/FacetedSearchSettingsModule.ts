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

import Axios from "axios";
import {
  BatchOperationResponse,
  groupErrorMessages,
} from "../../../api/BatchOperationResponse";

export interface FacetedSearchClassification {
  /**
   * Name of a classification
   */
  name: string;
  /**
   * Schema node of a classification
   */
  schemaNode: string;
  /**
   * The number of category of a classification; Being undefined means the number is unlimited.
   */
  maxResults?: number;
  /**
   * Used for re-ordering classifications.
   */
  orderIndex: number;
}

export interface ModifiedClassification extends FacetedSearchClassification {
  /**
   * A flag indicating creating or updating this classification on the Server.
   */
  changed: boolean;
  /**
   * A flag indicating deleting this classification from the Server.
   */
  deleted: boolean;
  /**
   * A flag indication if a classification hasn't been saved to the Server.
   */
  dirty: boolean;
}

const FACETED_SEARCH_API_URL = "api/settings/facetedsearch/classification";

export const getClassificationsFromServer = (): Promise<
  FacetedSearchClassification[]
> => Axios.get(FACETED_SEARCH_API_URL).then((res) => res.data);

export const batchUpdateOrAdd = (
  classifitioncas: FacetedSearchClassification[]
) =>
  Axios.put<BatchOperationResponse[]>(
    FACETED_SEARCH_API_URL,
    classifitioncas
  ).then((res) => groupErrorMessages(res.data));

/**
 * Validate if trimmed name or schema node is empty.
 * Return true if they are invalid.
 */
export const validateClassificationFields = (field: string): boolean => {
  return !field?.trim();
};

/**
 * Convert an instace of FacetedSearchClassification to an instace of ModifiedClassification.
 */
export const addFlags = (
  classification: FacetedSearchClassification,
  changed: boolean,
  deleted: boolean,
  dirty: boolean
): ModifiedClassification =>
  Object.assign(classification, {
    changed: changed,
    deleted: deleted,
    dirty: dirty,
  });

/**
 * Convert an instace of ModifiedClassification to an instace of FacetedSearchClassification.
 */
export const removeFlags = (classification: ModifiedClassification) => {
  delete classification.deleted;
  delete classification.changed;
  delete classification.dirty;
  return classification;
};

/**
 * Given a list of classifications, find the one that has the highest order index.
 */
export const getHighestOrderIndex = (
  classifications: ModifiedClassification[]
) => {
  const classification = classifications.reduce((prev, current) => {
    return prev.orderIndex > current.orderIndex ? prev : current;
  }, defaultClassifion);
  return classification.orderIndex;
};

const defaultClassifion: FacetedSearchClassification = {
  name: "",
  schemaNode: "",
  orderIndex: -1,
};
