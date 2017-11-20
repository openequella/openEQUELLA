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

package com.tle.web.bulk.executescript.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.BookmarkEvent;

@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
public class BulkExecuteScriptModel
{
	private boolean validationRan = false;
	@Bookmarked
	private boolean validationErrors;
	@Bookmarked
	private String errorMessage;

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public boolean isValidationErrors()
	{
		return validationErrors;
	}

	public void setValidationErrors(boolean validationErrors)
	{
		this.validationErrors = validationErrors;
	}

	public boolean isValidationRan()
	{
		return validationRan;
	}

	public void setValidationRan(boolean validationRan)
	{
		this.validationRan = validationRan;
	}

}