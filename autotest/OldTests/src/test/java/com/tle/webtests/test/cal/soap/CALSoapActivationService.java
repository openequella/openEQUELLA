package com.tle.webtests.test.cal.soap;

public interface CALSoapActivationService {
  /**
   * Activates the copyright on said attachments
   *
   * @param uuid Item uuid
   * @param version Item version
   * @param course Course code
   * @param attachments List of attachment uuids
   */
  void activateItemAttachments(String uuid, int version, String courseCode, String[] attachments)
      throws Exception;
}
