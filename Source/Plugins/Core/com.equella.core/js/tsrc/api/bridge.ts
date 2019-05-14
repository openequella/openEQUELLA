import * as React from "react";
import { Routes, Route } from "./routes";
import { AclEditorProps } from "./acleditor";
import { ClickableLink } from "./General";

export interface Bridge {
  routes: Routes;
  router: (route: Route) => ClickableLink;
  forcePushRoute: (Route: Route) => void;
  matchRoute: (uri: string) => Route | null;
  AclEditor: React.ComponentType<AclEditorProps>;
}
