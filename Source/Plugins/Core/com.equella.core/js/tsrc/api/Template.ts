import { ReactNode, ReactChild } from "react";
import { Route } from "./routes";

export interface TemplateProps {
    title: String;
    fixedViewPort?: Boolean;
    titleExtra? : ReactNode;
    menuExtra?: [ReactChild];
    children: ReactNode;
    tabs?: ReactNode;
    preventNavigation?: Boolean;
    backRoute?: Route;
}