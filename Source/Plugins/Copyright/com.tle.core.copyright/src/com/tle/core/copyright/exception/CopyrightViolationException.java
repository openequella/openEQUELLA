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

package com.tle.core.copyright.exception;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class CopyrightViolationException extends WorkflowException
{
	private static final long serialVersionUID = 1L;
	private LanguageBundle i18NMessage;
	private boolean calBookPercentageException;

	public CopyrightViolationException(LanguageBundle message)
	{
		super(CurrentLocale.get(message));
		this.i18NMessage = message;
	}

	public LanguageBundle getI18NMessage()
	{
		return i18NMessage;
	}

	@Override
	public String getLocalizedMessage()
	{
		return CurrentLocale.get(i18NMessage);
	}

	public boolean isCALBookPercentageException()
	{
		return calBookPercentageException;
	}

	public void setCALBookPercentageException(boolean percentageException)
	{
		this.calBookPercentageException = percentageException;
	}
}
