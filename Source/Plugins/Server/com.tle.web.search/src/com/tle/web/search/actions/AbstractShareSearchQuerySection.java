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

package com.tle.web.search.actions;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.mail.internet.AddressException;

import com.google.common.base.Strings;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.RunAsUser;
import com.tle.core.services.user.UserService;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.feeds.FeedServlet;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewurl.ViewItemUrlFactory;

@SuppressWarnings("nls")
public abstract class AbstractShareSearchQuerySection
	extends
		AbstractPrototypeSection<AbstractShareSearchQuerySection.ShareSearchModel>
{
	protected static final int RESULTS_CAP = 50;

	@PlugKey("share.search.email.")
	private static String PREFIX;
	@PlugKey("actions.share.dialog.email.subject")
	protected static String SUBJECT_KEY;
	@PlugKey("unknown.user")
	protected static String UNKNOWN_USER;

	@PlugKey("actions.share.dialog.email.validation.nonblank")
	protected static Label BLANK_EMAIL_LABEL;
	@PlugKey("actions.share.dialog.email.receipt")
	protected static Label RECEIPT_LABEL_OK;
	@PlugKey("actions.share.dialog.email.failreceipt")
	protected static Label RECEIPT_LABEL_FAIL;
	@PlugKey("actions.share.dialog.email.validation.format")
	protected static Label VALIDATION_EMAIL_FORMAT_LABEL;

	@Inject
	protected FeedServlet feedServlet;
	@Inject
	protected ItemService itemService;
	@Inject
	protected InstitutionService institutionService;
	@Inject
	protected ViewItemUrlFactory urlFactory;
	@Inject
	protected UserService userService;
	@Inject
	protected FreeTextService freeTextService;
	@Inject
	protected RunAsUser runAsUser;
	@Inject
	protected ReceiptService receiptService;
	@Inject
	protected EmailService emailService;

	@TreeLookup
	protected AbstractRootSearchSection<?> rootSearch;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResultsSection;

	@EventFactory
	protected EventGenerator events;

	@Component(name = "u")
	protected TextField url;
	@Component(name = "e")
	protected TextField email;
	@Component(name = "seb")
	@PlugKey("actions.share.dialog.email.send")
	protected Button sendEmailButton;

	@Nullable
	private AbstractShareSearchQueryDialog containerDialog;

	protected abstract String createEmail(SectionInfo info);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		JSValidator fail = email.createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_EMAIL_LABEL));
		sendEmailButton.setClickHandler(events.getNamedHandler("sendEmail").addValidator(fail));
	}

	@EventHandlerMethod
	public void sendEmail(SectionInfo info)
	{
		ShareSearchModel model = getModel(info);
		try
		{
			Future<EmailResult<String>> result = emailService.sendEmail(CurrentLocale.get(SUBJECT_KEY),
				emailService.parseAddresses(email.getValue(info)), createEmail(info));

			EmailResult<String> emailResult;
			emailResult = result.get();
			boolean successful = emailResult.isSuccessful();

			doSentMessage(info, successful ? RECEIPT_LABEL_OK : RECEIPT_LABEL_FAIL);

			if( containerDialog != null )
			{
				containerDialog.close(info);
			}
		}
		catch( AddressException ae )
		{
			model.setEmailProblem(VALIDATION_EMAIL_FORMAT_LABEL);
		}
		catch( InterruptedException | ExecutionException e )
		{
			receiptService.setReceipt(RECEIPT_LABEL_FAIL);
			if( containerDialog != null )
			{
				containerDialog.close(info);
			}
		}
	}

	protected void doSentMessage(SectionInfo info, Label message)
	{
		receiptService.setReceipt(message);
	}

	public void setContainerDialog(AbstractShareSearchQueryDialog containerDialog)
	{
		this.containerDialog = containerDialog;
	}

	protected void setupUrl(InfoBookmark bookmark, RenderContext context)
	{
		url.setValue(context, bookmark.getHref());
		url.getState(context).setEditable(false);
	}

	protected String s(String key, Object... args)
	{
		return CurrentLocale.get(PREFIX + key, args);
	}

	protected String getUser(UserBean ub)
	{
		if( ub == null )
		{
			return CurrentLocale.get(UNKNOWN_USER);
		}

		return MessageFormat.format("{0} {1} ({2})", Strings.nullToEmpty(ub.getFirstName()),
			Strings.nullToEmpty(ub.getLastName()), Strings.nullToEmpty(ub.getEmailAddress()).toString());
	}

	protected AbstractSearchResultsSection<?, ?, ?, ?> getSearchResultsSection()
	{
		return searchResultsSection;
	}

	@Override
	public ShareSearchModel instantiateModel(SectionInfo info)
	{
		return new ShareSearchModel();
	}

	public TextField getUrl()
	{
		return url;
	}

	public TextField getEmail()
	{
		return email;
	}

	public Button getSendEmailButton()
	{
		return sendEmailButton;
	}

	public static class ShareSearchModel
	{
		private Label emailProblem;
		private Label emailMessage;
		private boolean showEmail;

		public Label getEmailProblem()
		{
			return emailProblem;
		}

		public void setEmailProblem(Label emailProblem)
		{
			this.emailProblem = emailProblem;
		}

		public Label getEmailMessage()
		{
			return emailMessage;
		}

		public void setEmailMessage(Label emailMessage)
		{
			this.emailMessage = emailMessage;
		}

		public boolean isShowEmail()
		{
			return showEmail;
		}

		public void setShowEmail(boolean showEmail)
		{
			this.showEmail = showEmail;
		}
	}
}
