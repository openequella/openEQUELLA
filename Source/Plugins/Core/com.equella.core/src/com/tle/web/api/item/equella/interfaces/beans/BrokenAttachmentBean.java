package com.tle.web.api.item.equella.interfaces.beans;

/** Typically used when generating an instance of `AttachmentBean` for an Attachment fails. */
public class BrokenAttachmentBean extends EquellaAttachmentBean {

  private final String originalAttachmentType;

  public BrokenAttachmentBean(String uuid, String attachmentType, String desc) {
    setUuid(uuid);
    setDescription(desc);
    this.originalAttachmentType = attachmentType;
  }

  @Override
  public String getRawAttachmentType() {
    return originalAttachmentType;
  }
}
