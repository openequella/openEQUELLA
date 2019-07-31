export interface CloudProviderEntity {
  id: string;
  name: string;
  description?: string;
  iconUrl?: string;
  canRefresh: boolean;
}
