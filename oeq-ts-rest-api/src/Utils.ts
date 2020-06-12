import * as Lodash from "lodash";

/**
 * Return a copied object of which types of specified fields are converted to Date.
 * @param fields Names of fields that need type conversion.
 * @param data The object to be processed. Nested objects will be processed, too.
 * @param cloneData True if deep clone is required.
 */
export const convertDateFields = <T>(fields: string[], data: T, cloneData: boolean): unknown => {
  const clonedData: T = cloneData? Lodash.cloneDeep(data) : data;
  const results: [string, any][] = Object.entries(clonedData);

  results.forEach(([key, value]) => {
    if(typeof value === "object"){
      // Don't need to deep clone nested objects again.
      convertDateFields(fields, value, false);
    }
    else{
      fields.forEach(field => {
        // Convert when a date string can be parsed to Date.
        if(field === key && !isNaN(Date.parse(value))){
          (clonedData as any)[field] = new Date(value)
        }
      })
    }
  });

  return clonedData;
};
