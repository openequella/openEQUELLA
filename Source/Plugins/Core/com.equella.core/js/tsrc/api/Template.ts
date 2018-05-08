import { ReactNode, ReactChild } from "react";

export interface TemplateProps {
    title: String;
    fixedViewPort?: Boolean;
    titleExtra? : ReactNode;
    menuExtra?: [ReactChild];
    children: ReactNode;
    tabs?: ReactNode;
    preventNavigation?: Boolean;
}