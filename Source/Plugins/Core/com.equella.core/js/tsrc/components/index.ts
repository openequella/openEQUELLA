import * as React from "react";
import { generateFromError } from "../api/errors";
import { TemplateUpdateProps, templateError } from "../mainui/Template";

export { default as Error } from "./Error";
export { default as Loader } from "./Loader";
export { default as AppBarQuery } from "./AppBarQuery";
export { default as SearchResult } from "./SearchResult";

export function handleUnexpectedApiError<P extends TemplateUpdateProps>({
  props: { updateTemplate },
}: React.Component<P>) {
  return (err: Error) => updateTemplate(templateError(generateFromError(err)));
}
