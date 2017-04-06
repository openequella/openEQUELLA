package com.tle.web.activation.soap;

/**
 * The WSDL file for this service is available at
 * http://INSTITUTION_URL/services/calactivation.service?wsdl
 */
public interface SoapActivationService
{
	/**
	 * Activates the copyright on said attachments
	 * 
	 * @param uuid Item uuid
	 * @param version Item version
	 * @param courseCode Course code
	 * @param attachments List of attachment uuids
	 */
	void activateItemAttachments(String uuid, int version, String courseCode, String[] attachments) throws Exception;
}
