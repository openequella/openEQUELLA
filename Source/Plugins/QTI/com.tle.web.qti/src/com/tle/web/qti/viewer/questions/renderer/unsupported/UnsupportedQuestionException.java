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

package com.tle.web.qti.viewer.questions.renderer.unsupported;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Aaron
 */
public class UnsupportedQuestionException extends RuntimeException
{
	private final String nodeName;
	private final boolean element;

	public UnsupportedQuestionException(String nodeName)
	{
		this(nodeName, false);
	}

	/**
	 * @param element True if it is a sub-part of the question that can't be
	 *            rendered and not the interaction itself
	 */
	public UnsupportedQuestionException(String nodeName, boolean element)
	{
		this.nodeName = nodeName;
		this.element = element;
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		return CurrentLocale.get("com.tle.web.qti.notsupported." + (element ? "element" : "question"), nodeName);
	}
}
