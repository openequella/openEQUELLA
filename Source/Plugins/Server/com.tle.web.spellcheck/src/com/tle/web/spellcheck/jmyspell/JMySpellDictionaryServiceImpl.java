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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.java.plugin.registry.Extension;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.spellcheck.dictionary.DictionaryService;
import com.tle.web.spellcheck.dictionary.TLEDictionary;

@Bind(DictionaryService.class)
@Singleton
public class JMySpellDictionaryServiceImpl implements DictionaryService
{
	private Map<String, OpenOfficeSpellDictionary> dictionaries = new HashMap<String, OpenOfficeSpellDictionary>();
	private Map<String, TLEDictionary> dictMap;
	private Map<String, Extension> extMap;
	@Inject
	private PluginTracker<TLEDictionary> tracker;

	@Override
	public synchronized List<TLEDictionary> getTLEDictionaryList()
	{
		if( dictMap == null || tracker.needsUpdate() )
		{
			dictMap = new HashMap<String, TLEDictionary>();
			extMap = new HashMap<String, Extension>();
			for( Extension ex : tracker.getExtensions() )
			{
				TLEDictionary dictionary = new TLEDictionary();
				String langCode = ex.getParameter("code").valueAsString(); //$NON-NLS-1$
				dictionary.setCode(langCode);
				dictionary.setLanguage(ex.getParameter("language").valueAsString()); //$NON-NLS-1$
				dictionary.setFilename(ex.getParameter("filename").valueAsString()); //$NON-NLS-1$

				dictMap.put(langCode, dictionary);
				extMap.put(langCode, ex);
			}
		}
		return new ArrayList<TLEDictionary>(dictMap.values());
	}

	@Override
	public synchronized OpenOfficeSpellDictionary getDictionary(String code)
	{
		if( !dictionaries.containsKey(code) )
		{
			getTLEDictionaryList();
			TLEDictionary dict = dictMap.get(code);
			Extension ex = extMap.get(code);
			InputStream stream = tracker.getResourceAsStream(ex, dict.getFilename());

			/**
			 * for OpenOfficeSpellDictionary(InputStream zippedStream, File
			 * personalDict): It seems that personalDict is a personal
			 * dictionary file where users can add words to their ignore list,
			 * add new words etc. Since we don't use that, we can leave it
			 * empty. It also seems that the constructor method is not thread
			 * safe, which means since we do this in a loop the dictionaries
			 * arne't being built properly. The results will be
			 * NullPointerExceptions all over the place when we try to use the
			 * dictionary. Thus, run in background is set to false.
			 */
			OpenOfficeSpellDictionary oosd;
			try
			{
				oosd = new OpenOfficeSpellDictionary(stream, ((File) null), false);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			dictionaries.put(code, oosd);
		}
		return dictionaries.get(code);
	}
}
