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

package com.tle.core.email.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.settings.standard.MailSettings;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.system.SystemConfigService;

@Singleton
@Bind(EmailService.class)
@SuppressWarnings("nls")
public class EmailServiceImpl implements EmailService
{
	private static final String SMTP_AUTH_PROP = "mail.smtp.auth";
	private static final String SUBMITTER_PROP = "mail.smtp.submitter";
	private static final String XMAILER = "EQUELLA";
	private static final String HOST_PROP = "mail.host";
	private static final String FROM_PROP = "mail.from";
	private static final String PROTO_PROP = "mail.transport.protocol";
	private static final String TLS_PROP = "mail.smtp.starttls.enable";
	private static final String SMTP = "smtp";
	private static final String HTML_MIME_TYPE = "text/html; charset=UTF-8";
	private static final String TEXT_MIME_TYPE = "text/plain; charset=UTF-8";

	private final ExecutorService emailThread = Executors.newSingleThreadExecutor();

	@Inject
	private EncryptionService encryptionService;

	@Inject
	private ConfigurationService configService;

	@Inject
	private SystemConfigService systemConfigService;

	@Override
	public boolean isValidAddress(String emailAddress)
	{
		try
		{
			new InternetAddress(emailAddress).validate();
			return true;
		}
		catch( AddressException e )
		{
			return false;
		}
	}

	@Override
	public List<String> parseAddresses(String emails) throws AddressException
	{
		String[] emailsList = new String[0];
		String rawEmail = emails;
		if( rawEmail != null )
		{
			rawEmail = rawEmail.replaceAll(";", " ");
			emailsList = rawEmail.split("\\s+");
		}

		List<String> addresses = new ArrayList<String>();
		for( String email : emailsList )
		{
			new InternetAddress(email).validate();
			addresses.add(email);
		}
		return addresses;
	}

	@Override
	public <T> Callable<EmailResult<T>> createEmailer(String subject, List<String> emailAddresses, String message,
		final T key, MailSettings settings)
	{
		final String senderEmail = settings.getSender();
		InternetAddress senderAddr;
		try
		{
			senderAddr = new InternetAddress(senderEmail, settings.getSenderName(), Constants.UTF8);
			final Properties props = System.getProperties();
			String server = settings.getServer();
			if( Check.isEmpty(server) )
			{
				throw new RuntimeException(
					"Incorrect mail settings - No server set on institution: " + CurrentInstitution.get().getName());
			}
			int ind = server.indexOf(':');
			if (ind != -1)
			{
				props.put("mail.smtp.port", server.substring(ind+1));
				server = server.substring(0, ind);
			}
			props.put(HOST_PROP, server);
			props.put(FROM_PROP, senderEmail);
			props.put(PROTO_PROP, SMTP);
			props.put(TLS_PROP, "true");

			Session mailSession = null;
			Authenticator auth = getAuthenticator(settings);
			if( auth != null )
			{
				props.put(SMTP_AUTH_PROP, "true");
				props.put(SUBMITTER_PROP, auth.getPasswordAuthentication().getUserName());
				mailSession = Session.getInstance(props, auth);
			}
			else
			{
				props.remove(SMTP_AUTH_PROP);
				props.remove(SUBMITTER_PROP);
				mailSession = Session.getDefaultInstance(props, null);
			}

			Message mimeMessage = new MimeMessage(mailSession);
			mimeMessage.setFrom(senderAddr);

			for( String email : emailAddresses )
			{
				mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(email));
			}
			mimeMessage.setSubject(subject);
			mimeMessage.setHeader("X-Mailer", XMAILER);
			mimeMessage.setHeader("MIME-Version", "1.0");
			String type = TEXT_MIME_TYPE;
			// Hack
			if( message.contains("<html>") )
			{
				type = HTML_MIME_TYPE;
			}
			mimeMessage.setHeader("Content-Type", type);
			mimeMessage.setContent(message, type);
			return new EmailCallable<T>(mimeMessage, key);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
		catch( MessagingException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> Callable<EmailResult<T>> createEmailer(String subject, List<String> emailAddresses, String message,
		final T key)
	{
		return createEmailer(subject, emailAddresses, message, key, getMailSettings());
	}

	private MailSettings getMailSettings()
	{
		return configService.getProperties(new MailSettings());
	}

	private MailSettings getSystemMailSettings()
	{
		MailSettings mailSettings = new MailSettings();
		String smtp = systemConfigService.getSmtpServer();

		String smtpUser = systemConfigService.getSmtpUser();
		String smtpPassword = systemConfigService.getSmtpPassword();
		mailSettings.setServer(smtp);
		if (systemConfigService.getNoReplySender()!=null){
		    mailSettings.setSender(systemConfigService.getNoReplySender());
		}else{
            mailSettings.setSender("noReply@noreply.com");
        }
		if( !Check.isEmpty(smtpUser) )
		{
			mailSettings.setUsername(smtpUser);
			mailSettings.setPassword(smtpPassword);
		}

		return mailSettings;
	}

	private Authenticator getAuthenticator(final MailSettings settings)
	{
		String username = settings.getUsername();
		String password = settings.getPassword();
		if( !Check.isEmpty(username) && !Check.isEmpty(password) )
		{
			String dpwd = encryptionService.decrypt(password);
			return new Authenticator(username, dpwd);
		}

		return null;
	}

	@Override
	public Future<EmailResult<String>> sendEmail(String subject, List<String> emailAddresses, String message)
	{
		return emailThread.submit(createEmailer(subject, emailAddresses, message, message));
	}

	@Override
	public Future<EmailResult<String>> sendEmail(String subject, List<String> emailAddresses, String message,
		MailSettings settings, boolean enc)
	{
		// If it's a new password encrypt it
		if( enc )
		{
			settings.setPassword(encryptionService.encrypt(settings.getPassword()));
		}
		return emailThread.submit(createEmailer(subject, emailAddresses, message, message, settings));
	}

	public static class EmailCallable<T> implements Callable<EmailResult<T>>
	{
		private final Message message;
		private final T key;

		public EmailCallable(Message message, T key)
		{
			this.message = message;
			this.key = key;
		}

		@Override
		public EmailResult<T> call() throws Exception
		{
			try
			{
				Transport.send(message);
			}
			catch( Exception t )
			{
				return new EmailResult<T>(t, key);
			}
			return new EmailResult<T>(null, key);
		}

	}

	private static class Authenticator extends javax.mail.Authenticator
	{
		private final PasswordAuthentication authentication;

		public Authenticator(String username, String password)
		{
			authentication = new PasswordAuthentication(username, password);
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			return authentication;
		}
	}

	@Override
	public boolean hasMailSettings()
	{
		MailSettings ms = getMailSettings();
		return !Check.isEmpty(ms.getServer()) && !Check.isEmpty(ms.getSender());
	}

	@Override
	public Future<EmailResult<String>> sendSystemEmail(String subject, String message)
	{
		MailSettings mailSettings = getSystemMailSettings();
		String emailsText = systemConfigService.getEmails();
		String[] emails = emailsText.split(";");

		return emailThread.submit(createEmailer(subject, Lists.newArrayList(emails), message, message, mailSettings));
	}
}
