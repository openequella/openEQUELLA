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

	public InstallSettings(String hashedPassword)
	{
		this(hashedPassword, "", "", "", "");
	}

	public InstallSettings(String hashedPassword, String emailsText, String smtpServer, String smtpUser, String smtpPassword)
	{
		this.hashedPassword = hashedPassword;
		this.emailsText = emailsText;
		this.smtpServer = smtpServer;
		this.smtpUser = smtpUser;
		this.smtpPassword = smtpPassword;
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

}
