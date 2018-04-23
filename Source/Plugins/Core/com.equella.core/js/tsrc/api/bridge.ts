import { Routes, Route } from "./routes";
import { AclEditorProps } from "./acleditor";

export interface Bridge {
    routes: Routes,
    router: (route: Route) => {
        href: string;
        onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    },
    AclEditor: React.ComponentType<AclEditorProps>
}