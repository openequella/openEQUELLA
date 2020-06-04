import * as Common from './Common';

export enum ItemStatus {
  DRAFT = "DRAFT",
  LIVE = "LIVE",
  REJECTED = "REJECTED",
  MODERATING = "MODERATING",
  ARCHIVED = "ARCHIVED",
  SUSPENDED = "SUSPENDED",
  DELETED = "DELETED",
  REVIEW = "REVIEW",
  PERSONAL = "PERSONAL",
}

export interface DisplayFields {
  type: string;
  name: Common.i18nString;
  html: Common.i18nString;
}

export interface DisplayOptions {
  attachmentType: string;
  disableThumbnail: boolean;
  standardOpen: boolean;
  integrationOpen: boolean;
}

export interface Attachment {
  attachmentType: string;
  id: string;
  description?: string;
  preview: boolean;
  links: Record<string, string>;
}

export interface Item {
  uuid: string;
  name?: Common.i18nString;
  description?: Common.i18nString;
  status: string;
  createdDate: string;
  modifiedDate: string;
  collectionId: string;
  commentCount: number;
  attachments: Attachment[];
  thumbnail: string;
  displayFields: DisplayFields[];
  displayOptions?: DisplayOptions;
  links: Record<string, string>;
}
