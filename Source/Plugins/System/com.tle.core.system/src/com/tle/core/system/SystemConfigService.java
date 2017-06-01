/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    String getNoReplySender();

    void setNoReplySender(String noReplySender);

	String getServerMessage();

	void setServerMessage(String serverMessage, boolean serverMessageEnabled);

	boolean isServerMessageEnabled();

	String getScheduledTasksConfig();

	void setScheduleTasksConfig(String config);

	long createUniqueInstitutionId();

	void registerInstitutionIdInUse(long id);

	boolean isSystemSchemaUp();
}
