/*
 * Created on Mar 16, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import java.util.Calendar;
import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.common.hash.Hash.Digester;
import com.tle.core.guice.Bind;

@Bind
public class RemoteSupportWrapper extends AbstractSystemUserWrapper
{
	@Override
	protected boolean authenticatePassword(String suppliedPassword)
	{
		final String supportKey = ""; // FIXME
		if( Check.isEmpty(supportKey) )
		{
			return false;
		}

		final StringBuilder b = new StringBuilder(supportKey);

		final Calendar c = Calendar.getInstance();
		b.append(c.get(Calendar.YEAR));
		b.append(c.get(Calendar.MONTH) + 1);
		b.append(c.get(Calendar.DAY_OF_MONTH));

		final String expectedPass = Hash.rawHash(Digester.MD5, b.toString());
		return Objects.equals(suppliedPassword, expectedPass);
	}

	@Override
	protected String getEmailAddress()
	{
		return "support@thelearningedge.com.au"; //$NON-NLS-1$
	}
}
