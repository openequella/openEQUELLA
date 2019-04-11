import * as React from "react";
import { ErrorResponse, generateFromError } from "../api/errors";

export { default as Error } from "./Error";
export { default as Loader } from "./Loader";
export { default as AppBarQuery } from "./AppBarQuery";
export { default as SearchResult } from "./SearchResult";

export function handleUnexpectedApiError<P>(
  t: React.Component<P, { errorResponse?: ErrorResponse }>
): (err: any) => void {
  return function(err) {
    t.setState({ errorResponse: generateFromError(err) });
  };
}
