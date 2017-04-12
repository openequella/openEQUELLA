package com.tle.core.system;

import com.tle.exceptions.BadCredentialsException;

/**
 * @author Nicholas Read
 */
public interface SystemConfigService
{
	void setAdminPassword(String oldPass, String newPass);

	void checkAdminPassword(String password) throws BadCredentialsException;

	boolean adminPasswordNotSet();

	void setInitialAdminPassword(String newPass);

	String getLicense();

	void setLicense(String string);

	String getEmails();

	void setEmails(String emails);

	String getSmtpServer();

	void setSmtpServer(String smtp);

	String getSmtpUser();

	void setSmtpUser(String smtpUser);

	String getSmtpPassword();

	void setSmtpPassword(String smtpPassword);

	String getServerMessage();

	void setServerMessage(String serverMessage, boolean serverMessageEnabled);

	boolean isServerMessageEnabled();

	String getScheduledTasksConfig();

	void setScheduleTasksConfig(String config);

	long createUniqueInstitutionId();

	void registerInstitutionIdInUse(long id);

	boolean isSystemSchemaUp();
}