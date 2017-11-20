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

package com.tle.web.spellcheck.jmyspell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;

import com.tle.core.guice.Bind;
import com.tle.web.spellcheck.AbstractSpellcheckService;
import com.tle.web.spellcheck.SpellcheckRequest.SpellcheckRequestParams;
import com.tle.web.spellcheck.SpellcheckResponse;
import com.tle.web.spellcheck.SpellcheckService;
import com.tle.web.spellcheck.dictionary.DictionaryService;

@Bind(SpellcheckService.class)
@Singleton
public class JMySpellSpellcheckServiceImpl extends AbstractSpellcheckService
{
	private Map<String, SpellChecker> spells = new HashMap<String, SpellChecker>();
	@Inject
	private DictionaryService dictionaryService;

	@Override
	public SpellcheckResponse checkWords(SpellcheckRequestParams params) throws IOException
	{
		SpellcheckResponse response = new SpellcheckResponse();
		List<String> badWords = new ArrayList<String>();
		for( String txt : params.getStringList() )
		{
			if( !getSpellChecker(params).getDictionary().isCorrect(txt) )
			{
				badWords.add(txt);
			}
		}
		response.setResult(badWords);
		return response;
	}

	private SpellChecker getSpellChecker(SpellcheckRequestParams params)
	{
		String locale = params.getLang();
		if( !spells.containsKey(locale) )
		{
			OpenOfficeSpellDictionary dictionary = dictionaryService.getDictionary(locale);
			spells.put(locale, new SpellChecker(dictionary));
		}
		return spells.get(locale);
	}

	@Override
	public SpellcheckResponse getSuggestions(SpellcheckRequestParams params) throws IOException
	{
		if( params.getStringList().size() > 1 )
		{
			throw new RuntimeException("You can't ask for suggestions for more than one word at a time!"); //$NON-NLS-1$
		}

		SpellcheckResponse response = new SpellcheckResponse();
		response.setResult(getSpellChecker(params).getDictionary().getSuggestions(params.getStringList().get(0)));

		return response;
	}
}
