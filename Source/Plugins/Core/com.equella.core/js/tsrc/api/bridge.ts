import * as React from "react";
import { Routes, Route } from "./routes";
import { AclEditorProps } from "./acleditor";
import { ClickableLink } from "./General";

export interface Bridge {
  routes: Routes;
  router: (route: Route) => ClickableLink;
  routeURI: (route: Route) => string;
  pushRoute: (route: Route) => void;
  forcePushRoute: (Route: Route) => void;
  matchRoute: (uri: string) => Route | null;
  setPreventNav: (cb: (route: Route) => boolean) => void;
  AclEditor: React.ComponentType<AclEditorProps>;
}
