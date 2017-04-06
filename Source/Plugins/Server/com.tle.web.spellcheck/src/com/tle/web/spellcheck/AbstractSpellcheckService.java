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
