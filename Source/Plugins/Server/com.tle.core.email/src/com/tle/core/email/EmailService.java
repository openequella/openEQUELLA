package com.tle.core.email;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.mail.internet.AddressException;

import com.tle.beans.system.MailSettings;

public interface EmailService
{
	boolean isValidAddress(String emailAddress);

	/**
	 * @param emails A whitespace or ; delimited list of email addresses.
	 * @throws AddressException
	 */
	List<String> parseAddresses(String emails) throws AddressException;

	<T> Callable<EmailResult<T>> createEmailer(String subject, List<String> emailAddresses, String message, T key);

	<T> Callable<EmailResult<T>> createEmailer(String subject, List<String> emailAddresses, String message, T key,
		MailSettings settings);

	Future<EmailResult<String>> sendEmail(String subject, List<String> emailAddresses, String message);

	Future<EmailResult<String>> sendEmail(String subject, List<String> emailAddresses, String message,
		MailSettings settings);

	Future<EmailResult<String>> sendSystemEmail(String subject, String message);

	boolean hasMailSettings();
}
