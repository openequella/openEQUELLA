/*
 * Copyright 2019 Apereo
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

package com.tle.web.errors;

import javax.inject.Inject;

import com.dytech.edge.exceptions.QuietlyLoggable;
import com.tle.core.services.LoggingService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public abstract class AbstractErrorSection<M extends AbstractErrorSection.ErrorModel>
	extends
		AbstractPrototypeSection<M> implements HtmlRenderer
{
	protected LoggingService loggingService;

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public final SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		Throwable matchedEx = (Throwable) context.getAttribute(SectionInfo.KEY_MATCHED_EXCEPTION);
		Throwable origEx = context.getAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION);

		final M model = getModel(context);
		if( origEx instanceof QuietlyLoggable )
		{
			QuietlyLoggable ql = (QuietlyLoggable) origEx;
			model.setNoLog(ql.isSilent());
			model.setNoStack(!ql.isShowStackTrace());
			model.setWarnOnly(ql.isWarnOnly());
		}
		model.setException(matchedEx);

		return renderErrorHtml(model, context);
	}

	@Inject
	public void setLoggingService(LoggingService loggingService)
	{
		this.loggingService = loggingService;
	}

	public abstract SectionResult renderErrorHtml(M model, RenderEventContext context) throws Exception;

	public static class ErrorModel
	{
		private Throwable exception;
		private boolean noLog;
		private boolean noStack;
		private boolean warnOnly;

		public Throwable getException()
		{
			return exception;
		}

		public void setException(Throwable exception)
		{
			this.exception = exception;
		}

		public boolean isNoLog()
		{
			return noLog;
		}

		public void setNoLog(boolean noLog)
		{
			this.noLog = noLog;
		}

		public boolean isNoStack()
		{
			return noStack;
		}

		public void setNoStack(boolean noStack)
		{
			this.noStack = noStack;
		}

		public boolean isWarnOnly()
		{
			return warnOnly;
		}

		public void setWarnOnly(boolean warnOnly)
		{
			this.warnOnly = warnOnly;
		}
	}
}
