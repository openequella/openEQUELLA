import * as React from "react";
import { AclEditorProps } from "./acleditor";
import { TemplateProps } from "tsrc/mainui/Template";

interface GenericPageProps {
  updateTemplate: (update: (template: TemplateProps) => TemplateProps) => void;
}

interface SettingsPageProps extends GenericPageProps {
  refreshUser: () => void;
}

export interface Bridge {
  AclEditor: React.ComponentType<AclEditorProps>;
  SettingsPage: React.ComponentType<SettingsPageProps>;
  SearchPage: React.ComponentType<GenericPageProps>;
}
