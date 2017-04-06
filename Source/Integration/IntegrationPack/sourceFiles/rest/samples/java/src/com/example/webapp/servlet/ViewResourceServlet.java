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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.node.ObjectNode;

import com.example.service.EquellaApiService;
import com.example.service.JsonMapper;
import com.example.webapp.model.ModelUtils;
import com.example.webapp.model.ViewResourceModel;

/**
 * /view Views information and attachments of an existing EQUELLA resource
 */
public class ViewResourceServlet extends AbstractServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected String getTemplateFilename()
	{
		return "view.ftl";
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		final HttpSession session = request.getSession();
		final String token = (String) session.getAttribute(KEY_TOKEN);

		final String uuid = getParameter(request, "uuid", null);
		final int version = getParameter(request, "version", 0);

		// If no UUID supplied then throw an error
		if( uuid == null )
		{
			throw new IllegalArgumentException("'uuid' parameter must be supplied to this page");
		}

		final ViewResourceModel model = new ViewResourceModel();
		model.setUrlContext(request.getContextPath());

		// Load the resource
		final ObjectNode resourceNode = EquellaApiService.getResource(uuid, version, "all", token);

		// Fill in the displayed fields
		model.setName(JsonMapper.getString(resourceNode, "name", uuid));
		model.setDescription(JsonMapper.getString(resourceNode, "description", null));

		final ObjectNode ownerNode = JsonMapper.getObject(resourceNode, "owner");
		if( ownerNode != null )
		{
			model.setOwner(JsonMapper.getString(ownerNode, "id", null));
		}
		model.setModified(JsonMapper.getDate(resourceNode, "modifiedDate"));
		model.setAttachments(ModelUtils.convertToAttachmentModels(resourceNode));

		// Render the page
		renderTemplate(response, model);
	}
}
