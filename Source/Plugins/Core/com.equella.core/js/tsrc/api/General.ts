export interface PagingResults<T> {
  start: number;
  length: number;
  available: number;
  resumptionToken?: string;
  results: T[];
}
