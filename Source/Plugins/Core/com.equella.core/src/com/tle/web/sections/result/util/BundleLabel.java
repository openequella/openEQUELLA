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

package com.tle.web.sections.result.util;

import java.util.Collection;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.InternalI18NString;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.TextBundle;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.PreRenderable;

public class BundleLabel implements Label, InternalI18NString
{
	protected final Object bundle;
	protected Label defaultLabel;
	protected BundleCache bundleCache;
	protected String defaultString = ""; //$NON-NLS-1$
	protected Collection<?> values;
	private Boolean html;

	public BundleLabel(Object bundle, BundleCache bundleCache)
	{
		this(bundle, (String) null, bundleCache);
	}

	public BundleLabel(Object bundle, String defaultString, BundleCache bundleCache)
	{
		this.bundle = bundle;
		this.bundleCache = bundleCache;
		bundleCache.addBundle(bundle);

		setDefaultString(defaultString);
	}

	public BundleLabel(Object bundle, Label defaultLabel, BundleCache bundleCache)
	{
		this.bundle = bundle;
		this.bundleCache = bundleCache;
		this.defaultLabel = defaultLabel;
		this.defaultString = null;
		bundleCache.addBundle(bundle);
	}

	public void setDefaultString(String defaultString)
	{
		this.defaultString = defaultString;
	}

	@Override
	public String getText()
	{
		String text = TextBundle.getLocalString(bundle, bundleCache, values, defaultString);
		if( text == null )
		{
			text = (defaultLabel == null ? "" : defaultLabel.getText()); //$NON-NLS-1$
		}
		return text;
	}

	@Override
	public String toString()
	{
		return getText();
	}

	@Override
	public boolean isHtml()
	{
		return html == null ? bundle instanceof String : html;
	}

	public BundleLabel setHtml(boolean html)
	{
		this.html = html;
		return this;
	}

	public static PreRenderable setupGlobalText(String key)
	{
		return new StringValuePrerenderable(key);
	}

	private static class StringValuePrerenderable implements PreRenderable
	{
		private final String bundleKey;

		public StringValuePrerenderable(String bundleKey)
		{
			this.bundleKey = bundleKey;
		}

		@SuppressWarnings("nls")
		@Override
		public void preRender(PreRenderContext info)
		{
			String var = "i18n_" + bundleKey.replace(".", "_");
			info.addStatements(
				new AssignStatement(new ScriptVariable(var), new StringExpression(CurrentLocale.get(bundleKey))));
		}
	}
}
