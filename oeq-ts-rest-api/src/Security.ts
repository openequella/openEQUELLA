export interface TargetListEntry {
  granted: boolean;
  override: boolean;
  privilege: string;
  who: string;
}

export interface BaseEntitySecurity {
  rules: TargetListEntry[];
}

export interface ItemMetadataSecurity {
  name: string;
  script: string
  entries: TargetListEntry[];
}

export interface DynamicRule {
  name: string;
  path: string;
  type: string;
  targetList:  TargetListEntry[];
}
