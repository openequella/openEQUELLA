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

package com.tle.web.workflow.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.sections.SectionsController;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@Singleton
public class WorkflowMessageFileServlet extends HttpServlet
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private SectionsController sectionsController;
	@Inject
	private InstitutionService institutionService;

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		if( CurrentUser.isGuest() || CurrentInstitution.get() == null )
		{
			StringBuilder requestUrl = new StringBuilder(
					institutionService.removeInstitution(request.getRequestURL().toString()));
			String queryString = request.getQueryString();
			if (queryString != null)
			{
				requestUrl.append('?').append(queryString);
			}
			LogonSection.forwardToLogon(sectionsController, request, response, requestUrl.toString(), LogonSection.STANDARD_LOGON_PATH);
			return;
		}
		String path = request.getPathInfo();

		int i = path.indexOf('/', 1);

		if( i < 0 )
		{
			throw new NotFoundException(path, true);
		}

		if( path.startsWith("/") )
		{
			path = path.substring(1);
		}

		int firstPart = path.indexOf('/');
		if( firstPart < 0 )
		{
			throw new NotFoundException(path, true);
		}

		String substring = path.substring(0, firstPart);

		if( substring.equals("$") )
		{
			path = path.substring(firstPart + 1);
			firstPart = path.indexOf('/');
			String stagingUuid = path.substring(0, firstPart);
			String filePath = path.substring(firstPart + 1);
			FileContentStream stream = fileSystemService.getContentStream(new StagingFile(stagingUuid), filePath,
				mimeService.getMimeTypeForFilename(filePath));
			contentStreamWriter.outputStream(request, response, stream);
		}
		else
		{
			String workflowMessageFilePath = path.substring(firstPart + 1);
			String uuid = substring;
			String mimetype = mimeService.getMimeTypeForFilename(workflowMessageFilePath);
			WorkflowMessageFile handle = new WorkflowMessageFile(uuid);
			FileContentStream stream = fileSystemService.getContentStream(handle, workflowMessageFilePath, mimetype);
			contentStreamWriter.outputStream(request, response, stream);
		}
	}
}