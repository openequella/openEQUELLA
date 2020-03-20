export interface GeneralSetting {
  id: string;
  group: string;
  name: string;
  description: string;
  links: {
    href?: string;
    route?: string;
  };
}

export interface UISetting {
  newUI: {
    enabled: boolean;
    newSearch: boolean;
  };
}
