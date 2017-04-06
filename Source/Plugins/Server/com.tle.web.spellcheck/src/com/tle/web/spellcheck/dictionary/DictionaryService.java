package com.tle.web.spellcheck.dictionary;

import java.util.List;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;

public interface DictionaryService
{
	/**
	 * @return List of language and codes. Should probably be Locales, but...
	 */
	List<TLEDictionary> getTLEDictionaryList();

	OpenOfficeSpellDictionary getDictionary(String code);
}
