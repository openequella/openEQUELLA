/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.webapp.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.example.service.EquellaApiService;
import com.example.service.JsonMapper;
import com.example.webapp.model.SearchModel;
import com.example.webapp.model.SearchResultModel;

/**
 * /search
 */
public class SearchServlet extends AbstractServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected String getTemplateFilename()
	{
		return "search.ftl";
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final HttpSession session = request.getSession();
		final String token = (String) session.getAttribute(KEY_TOKEN);

		final SearchModel model = new SearchModel();
		model.setUrlContext(request.getContextPath());
		model.setQuery(getParameter(request, "query", ""));
		model.setWhere(getParameter(request, "where", ""));
		model.setShowall(getParameter(request, "showall", false));
		model.setSorttype(getParameter(request, "sorttype", "relevance"));
		model.setReversesort(getParameter(request, "reversesort", false));
		model.setOffset(getParameter(request, "offset", 0));
		model.setMaxresults(getParameter(request, "maxresults", 10));

		if( getParameter(request, "search", null) != null )
		{
			final ObjectNode rootNode = EquellaApiService.search(model.getQuery(), null, model.getWhere(),
				model.getOffset(), model.getMaxresults(), model.getSorttype(), model.isReversesort(),
				model.isShowall(), "basic", token);

			final List<SearchResultModel> resultList = new ArrayList<SearchResultModel>();
			final JsonNode resultsNode = rootNode.get("results");
			for( JsonNode resultNode : resultsNode )
			{
				final String uuid = JsonMapper.getString(resultNode, "uuid", null);
				final int version = JsonMapper.getInt(resultNode, "version", 0);

				final SearchResultModel result = new SearchResultModel();
				result.setName(JsonMapper.getString(resultNode, "name", uuid));
				result.setUrl(request.getContextPath() + "/view?uuid=" + uuid + "&version=" + version);
				resultList.add(result);
			}
			model.setResults(resultList);

			final int start = JsonMapper.getInt(rootNode, "start", 0);
			model.setStart(start + 1);
			model.setEnd(start + JsonMapper.getInt(rootNode, "length", 0));
			model.setAvailable(JsonMapper.getInt(rootNode, "available", 0));
		}

		renderTemplate(response, model);
	}
}
