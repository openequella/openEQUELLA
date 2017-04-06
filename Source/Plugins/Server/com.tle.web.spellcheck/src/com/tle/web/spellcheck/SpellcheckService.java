package com.tle.web.spellcheck;

import java.io.IOException;

import com.tle.web.spellcheck.SpellcheckRequest.SpellcheckRequestParams;

public interface SpellcheckService
{
	SpellcheckResponse service(SpellcheckRequest request) throws IOException;

	SpellcheckResponse checkWords(SpellcheckRequestParams params) throws IOException;

	SpellcheckResponse getSuggestions(SpellcheckRequestParams params) throws IOException;
}
