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

package com.tle.core.institution;

import java.io.Serializable;

import com.tle.common.i18n.InternalI18NString;

public class InstitutionValidationError implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final InternalI18NString message;

	public InstitutionValidationError(String id, InternalI18NString message)
	{
		this.id = id;
		this.message = message;
	}

	public String getId()
	{
		return id;
	}

	public InternalI18NString getMessage()
	{
		return message;
	}
}
