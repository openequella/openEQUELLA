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

package com.tle.core.email;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.mail.internet.AddressException;

import com.tle.common.settings.standard.MailSettings;

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
		MailSettings settings, boolean enc);

	Future<EmailResult<String>> sendSystemEmail(String subject, String message);

	boolean hasMailSettings();
}
