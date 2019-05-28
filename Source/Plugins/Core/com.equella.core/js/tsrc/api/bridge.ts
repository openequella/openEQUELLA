import * as React from "react";
import { AclEditorProps } from "./acleditor";
import { ErrorResponse } from "./errors";
import { TemplateProps } from "tsrc/mainui/Template";

interface LegacyURI {
  must: string;
}

interface PageContent {
  html: { [key: string]: string };
  script: string;
  title: string;
  contentId: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  afterHtml: () => void;
}
interface LegacyContentProps {
  page: LegacyURI;
  contentUpdated: (content: PageContent) => void;
  userUpdated: () => void;
  redirected: (redir: { href: string; external: boolean }) => void;
  onError: (cb: { error: ErrorResponse; fullScreen: boolean }) => void;
}

interface GenericPageProps {
  updateTemplate: (update: (template: TemplateProps) => TemplateProps) => void;
}

interface SettingsPageProps extends GenericPageProps {
  refreshUser: () => void;
}

export interface Bridge {
  AclEditor: React.ComponentType<AclEditorProps>;
  LegacyContent: React.ComponentType<LegacyContentProps>;
  SettingsPage: React.ComponentType<SettingsPageProps>;
  SearchPage: React.ComponentType<GenericPageProps>;
  legacyUri: (uri: string) => LegacyURI;
}
