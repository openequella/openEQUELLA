import { ReactNode, ReactChild } from "react";
import { Route } from "./routes";
import { ErrorResponse } from "./errors";

export interface TemplateProps {
    title: String;
    /* Fix the height of the main content, otherwise use min-height */
    fixedViewPort?: Boolean;
    /* Extra part of the App bar (e.g. Search control) */
    titleExtra? : ReactNode;
    /* Extra menu options */
    menuExtra?: [ReactChild];
    /* The main content */
    children: ReactNode;
    /* Additional markup for displaying tabs which integrate with the App bar */
    tabs?: ReactNode;
    /* Prevent navigation away from this page (e.g. Unsaved data) */
    preventNavigation?: Boolean;
    /* An optional Route for showing a back icon button */
    backRoute?: Route;
    /* Markup to show at the bottom of the main area. E.g. save/cancel options */
    footer?: ReactNode;
    /* Unexpected errors can be displayed by setting this property */
    errorResponse?: ErrorResponse;
}