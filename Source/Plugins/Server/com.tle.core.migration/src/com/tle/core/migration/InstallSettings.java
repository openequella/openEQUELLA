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

package com.tle.core.migration;

import java.io.Serializable;

public class InstallSettings implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String hashedPassword;
	private final String emailsText;
	private final String smtpServer;
	private final String smtpUser;
	private final String smtpPassword;
    private final String noReplySender;

	public InstallSettings(String hashedPassword)
	{
		this(hashedPassword, "", "", "", "", "");
	}

	public InstallSettings(String hashedPassword, String emailsText, String smtpServer, String smtpUser, String smtpPassword, String noReplySender)
	{
		this.hashedPassword = hashedPassword;
		this.emailsText = emailsText;
		this.smtpServer = smtpServer;
		this.smtpUser = smtpUser;
		this.smtpPassword = smtpPassword;
        this.noReplySender = noReplySender;
	}


	public String getHashedPassword()
	{
		return hashedPassword;
	}

	public String getEmailsText()
	{
		return emailsText;
	}

	public String getSmtpServer()
	{
		return smtpServer;
	}

	public String getSmtpUser()
	{
		return smtpUser;
	}

	public String getSmtpPassword()
	{
		return smtpPassword;
	}

    public String getNoReplySender()
    {
        return noReplySender;
    }

}
