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

package com.tle.web.spellcheck;

import java.io.IOException;

import com.tle.web.spellcheck.SpellcheckRequest.SpellcheckRequestParams;

public abstract class AbstractSpellcheckService implements SpellcheckService
{
	@Override
	public abstract SpellcheckResponse checkWords(SpellcheckRequestParams params) throws IOException;

	@Override
	public abstract SpellcheckResponse getSuggestions(SpellcheckRequestParams params) throws IOException;

	@Override
	public SpellcheckResponse service(SpellcheckRequest request) throws IOException
	{
		SpellcheckResponse response = null;
		if( request.getMethod().equals("checkWords") ) //$NON-NLS-1$
		{
			response = checkWords(request.getParams());
		}
		else if( request.getMethod().equals("getSuggestions") ) //$NON-NLS-1$
		{
			response = getSuggestions(request.getParams());
		}
		else
		{
			throw new RuntimeException("I have no idea what you seek service for!");
		}

		return response;
	}

}
