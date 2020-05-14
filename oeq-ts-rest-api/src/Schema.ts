import * as Common from './Common';
import { GET } from './AxiosInstance';
import { is } from 'typescript-is';

export interface Citation {
  name: string;
  transformation: string;
}

export interface SchemaNode {
  children: Record<string, SchemaNode>;
  indexed: boolean;
  field: boolean;
  nested: boolean;
  type: string;
}

export interface Schema extends Common.BaseEntity {
  namePath: string;
  descriptionPath: string;
  /**
   * Typically a tree of objects representing an XML schema - so first entry is normally "xml".
   */
  definition: Record<string, unknown>;
}

export interface EquellaSchema extends Schema {
  citations: Citation[];
  exportTransformsMap: Record<string, string>;
  importTransformsMap: Record<string, string>;
  ownerUuid: string;
  serializedDefinition: string;
}

/**
 * Query params for listing of schemas. All are optional!
 */
export interface ListSchemaParams {
  /**
   * Search name and description
   */
  q?: string;
  /**
   * Privilege(s) to filter by
   */
  privilege?: string[];
  /**
   * Resumption token for paging
   */
  resumptionToken?: string;
  /**
   * Number of results
   */
  length?: number;
  /**
   * Return full entity (needs VIEW or EDIT privilege)
   */
  full?: boolean;
}

/**
 * Helper function for a standard validator for EquellaSchema instances via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isEquellaSchema = (instance: unknown): boolean =>
  is<EquellaSchema>(instance);

/**
 * Helper function for a standard validator for EquellaSchema instances wrapped in a PagedResult
 * via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isPagedEquellaSchema = (instance: unknown): boolean =>
  is<Common.PagedResult<EquellaSchema>>(instance);

const SCHEMA_ROOT_PATH = '/schema';

/**
 * List all available schemas which the currently authenticated user has access to. Results can
 * be customised based on params, and if the `full` param is specified then the return value is
 * actually EquellaSchema with all details.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param params Query parameters to customize (and/or page) result
 */
export const listSchemas = (
  apiBasePath: string,
  params: ListSchemaParams | null
): Promise<Common.PagedResult<Common.BaseEntity>> => {
  // Only if the `full` param is specified do you get a whole Schema definition, otherwise
  // it's the bare minimum of BaseEntity.
  const validator = params?.full
    ? isPagedEquellaSchema
    : Common.isPagedBaseEntity;

  return GET<Common.PagedResult<Common.BaseEntity>>(
    apiBasePath + SCHEMA_ROOT_PATH,
    validator,
    params ?? undefined
  );
};

export const getSchema = (
  apiBasePath: string,
  uuid: string
): Promise<EquellaSchema> =>
  GET<EquellaSchema>(
    apiBasePath + `${SCHEMA_ROOT_PATH}/${uuid}`,
    isEquellaSchema
  );
