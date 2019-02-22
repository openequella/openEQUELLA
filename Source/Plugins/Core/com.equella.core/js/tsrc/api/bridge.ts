import { Routes, Route } from "./routes";
import { AclEditorProps } from "./acleditor";
import { TemplateProps } from "./Template";
import { ClickableLink } from "./General";

export interface Bridge {
  routes: Routes;
  router: (route: Route) => ClickableLink;
  forcePushRoute: (Route: Route) => void;
  Template: React.ComponentType<TemplateProps>;
  AclEditor: React.ComponentType<AclEditorProps>;
}
