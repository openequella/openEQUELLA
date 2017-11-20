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

package com.tle.web.spellcheck.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.spellcheck.SpellcheckRequest;
import com.tle.web.spellcheck.SpellcheckRequest.SpellcheckRequestParams;
import com.tle.web.spellcheck.SpellcheckResponse;
import com.tle.web.spellcheck.SpellcheckService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class SpellcheckServlet extends HttpServlet
{
	@Inject
	private SpellcheckService spellcheckService;

	private static final long serialVersionUID = 1L;

	@Override
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String spellRequestJSON = req.getReader().readLine();

		JSONObject jsonReqObject = JSONObject.fromObject(spellRequestJSON);

		jsonReqObject.get("params");

		SpellcheckRequest spellRequest = new SpellcheckRequest();
		spellRequest.setId(jsonReqObject.getString("id"));
		spellRequest.setMethod(jsonReqObject.getString("method"));
		SpellcheckRequestParams params = new SpellcheckRequestParams();
		params.setLang(jsonReqObject.getJSONArray("params").getString(0));
		Object object = jsonReqObject.getJSONArray("params").get(1);
		if( object instanceof String )
		{
			params.setStringList(Collections.singletonList(object.toString()));
		}
		else if( object instanceof JSONArray )
		{
			params.setStringList((JSONArray) object);
		}
		spellRequest.setParams(params);

		SpellcheckResponse spellResponse = spellcheckService.service(spellRequest);

		resp.setHeader("Cache-Control", "no-cache, no-store"); //$NON-NLS-1$//$NON-NLS-2$
		resp.setContentType("application/json"); //$NON-NLS-1$
		resp.getWriter().write(parseResponseToJSON(spellResponse));
	}

	/**
	 * Parses a SpellcheckResponse object to a JSON String that the TinyMCE
	 * spellchecker can understand. This is because JSONlib returns a JSON that
	 * somehow TinyMCE doesn't like.
	 * 
	 * @param response
	 * @return JSONString
	 */
	private String parseResponseToJSON(SpellcheckResponse response)
	{
		String string = "";

		String responseId = "null";
		if( !Check.isEmpty(response.getId()) )
		{
			responseId = response.getId();
		}

		string += "{";
		string += "\"id\":" + responseId;
		string += ",";
		String resultsText = listToJSONText(response.getResult());
		if( resultsText.equals("null") )
		{
			resultsText = "[]";
		}
		string += "\"result\":" + resultsText;
		string += ",";
		string += "\"error\":" + listToJSONText(response.getError());
		string += "}";

		return string;
	}

	private String listToJSONText(List<String> list)
	{
		if( Check.isEmpty(list) )
		{
			return "null";
		}

		StringBuilder returnString = new StringBuilder();
		returnString.append("[");
		for( String msg : list )
		{
			returnString.append("\"");
			returnString.append(msg);
			returnString.append("\",");
		}
		returnString.deleteCharAt(returnString.length() - 1);
		returnString.append("]");
		return returnString.toString();

	}

	public void setSpellcheckService(SpellcheckService spellcheckService)
	{
		this.spellcheckService = spellcheckService;
	}
}
