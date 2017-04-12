package com.tle.web.sections.equella.receipt;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.render.Label;

@Bind(ReceiptService.class)
@Singleton
public class ReceiptServiceImpl implements ReceiptService
{
	@SuppressWarnings("nls")
	private static final String SESSION_KEY = "receipt";

	@Inject
	private UserSessionService session;

	@SuppressWarnings("nls")
	@Override
	public void setReceipt(Label receipt)
	{
		if( !(receipt instanceof Serializable) )
		{
			throw new UnsupportedOperationException("Use a serializable Label like KeyLabel");
		}
		session.setAttribute(SESSION_KEY, receipt);
	}

	@Override
	public Label getReceipt()
	{
		Label rv = session.getAttribute(SESSION_KEY);
		session.removeAttribute(SESSION_KEY);
		return rv;
	}
}
