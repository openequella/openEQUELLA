export interface TargetListEntry {
  granted: boolean;
  override: boolean;
  privilege: string;
  who: string;
}

export interface BaseEntitySecurity {
  rules: TargetListEntry[];
}
