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

package com.tle.core.connectors.reporting;

import java.sql.ParameterMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorContent.ConnectorContentAttribute;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.SearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.SimpleResultSet;
import com.tle.core.reporting.SimpleTypeQuery;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;
import com.tle.reporting.MetadataBean.Definition;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ConnectorQueryDelegate extends SimpleTypeQuery
{
	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService connectorRepoService;
	@Inject
	private AttachmentResourceService attachmentResoureService;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private SectionsController sectionsController;

	private volatile MetadataBean resultsMetadata;
	private volatile MetadataBean parameterMetadata;

	@Override
	public Map<String, ?> getDatasourceMetadata() throws OdaException
	{
		return Maps.newHashMap();
	}

	private IParameterMetaData getParameterMetadataBean()
	{
		if( parameterMetadata == null )
		{
			synchronized( this )
			{
				if( parameterMetadata == null )
				{
					parameterMetadata = new MetadataBean();
					addParameterDef("query", TYPE_STRING, parameterMetadata);
					addParameterDef("connectorId", TYPE_STRING, parameterMetadata);
					addParameterDef("courseId", TYPE_STRING, parameterMetadata);
					addParameterDef("folderId", TYPE_STRING, parameterMetadata);
					addParameterDef("hidden", TYPE_BOOLEAN, parameterMetadata);
				}
			}
		}
		return parameterMetadata;
	}

	private MetadataBean getResultsMetadataBean()
	{
		if( resultsMetadata == null )
		{
			synchronized( this )
			{
				if( resultsMetadata == null )
				{
					resultsMetadata = new MetadataBean();
					addColumn("uuid", TYPE_STRING, resultsMetadata);
					addColumn("version", TYPE_INT, resultsMetadata);
					addColumn("attachmentUuid", TYPE_STRING, resultsMetadata);
					addColumn("attachmentUrl", TYPE_STRING, resultsMetadata);
					addColumn("equellaUrl", TYPE_STRING, resultsMetadata);
					addColumn("externalTitle", TYPE_STRING, resultsMetadata);
					addColumn("externalUrl", TYPE_STRING, resultsMetadata);
					addColumn("courseName", TYPE_STRING, resultsMetadata);
					addColumn("courseCode", TYPE_STRING, resultsMetadata);
					addColumn("folder", TYPE_STRING, resultsMetadata);
					addColumn("dateAdded", TYPE_DATE, resultsMetadata);
					addColumn("dateModified", TYPE_DATE, resultsMetadata);
					addColumn("fileType", TYPE_STRING, resultsMetadata);
					addColumn("fileSize", TYPE_INT, resultsMetadata);
					addColumn("available", TYPE_BOOLEAN, resultsMetadata);

					// different connectors may or may not return this data
					addColumn("instructor", TYPE_STRING, resultsMetadata);
					addColumn("dateAccessed", TYPE_DATE, resultsMetadata);
					addColumn("enrollments", TYPE_INT, resultsMetadata);
				}
			}
		}

		return resultsMetadata;
	}

	@Override
	public IParameterMetaData getParameterMetadata(String query, List<Object> params) throws OdaException
	{
		return getParameterMetadataBean();
	}

	protected Definition addParameterDef(String name, int type, MetadataBean bean)
	{
		final Definition definition = addColumn(name, type, bean);
		definition.setMode(ParameterMetaData.parameterModeIn);
		return definition;
	}

	@Override
	public IResultSetExt executeQuery(String query, List<Object> params, int maxRows) throws OdaException
	{
		final Map<String, Object> paramNameValues = Maps.newHashMap();
		if( params != null && !Check.isEmpty(query) )
		{
			final String[] paramNames = query.split("\\|");
			// these should match up... big troubs if they don't
			for( int i = 0; i < paramNames.length; i++ )
			{
				paramNameValues.put(paramNames[i], params.get(i));
			}
		}

		final String connectorId = (String) paramNameValues.get("connectorId");
		String courseId = (String) paramNameValues.get("courseId");
		if( courseId != null && courseId.equals("all") )
		{
			courseId = "";
		}
		String folderId = (String) paramNameValues.get("folderId");
		if( folderId != null && folderId.equals("all") )
		{
			folderId = "";
		}
		String textQuery = (String) paramNameValues.get("query");
		if( textQuery != null && textQuery.equals("*") )
		{
			textQuery = "";
		}
		boolean showHidden = false;
		// Handle String or Boolean representations. It *should* be Boolean, but
		// is being
		// defaulted to String in Birt. TODO: Need to investigate.
		Object hidden = paramNameValues.get("hidden");
		if( hidden != null )
		{
			if( hidden instanceof Boolean )
			{
				showHidden = (Boolean) hidden;
			}
			else if( hidden instanceof String )
			{
				showHidden = Utils.parseLooseBool((String) hidden, false);
			}
		}

		Connector conn = null;
		if( connectorId != null && !connectorId.equals("all") )
		{
			conn = connectorService.getByUuid(connectorId);
			if( conn == null )
			{
				throw new OdaException(
					CurrentLocale.get("com.tle.core.connectors.querydelegate.error.connectornotfound"));
			}
		}

		final SearchResults<ConnectorContent> content;
		try
		{
			// if maxRows == 1 then fake it up (it's Birt asking for column
			// metadata)
			if( maxRows == 1 )
			{
				return fakeData();
			}
			if( conn == null )
			{
				content = connectorRepoService.findAllUsagesAllConnectors(CurrentUser.getUsername(), textQuery,
					courseId, folderId, showHidden, 0, maxRows, ExternalContentSortType.DATE_ADDED, false);
			}
			else
			{
				content = connectorRepoService.findAllUsages(conn, CurrentUser.getUsername(), textQuery, courseId,
					folderId, showHidden, 0, maxRows, ExternalContentSortType.DATE_ADDED, false);
			}
		}
		catch( LmsUserNotFoundException lmsu )
		{
			throw new OdaException(CurrentLocale.get("querydelegate.error.usernotfound", lmsu.getUsername()));
		}

		return convertConnectorContentList(content.getResults());
	}

	private IResultSetExt convertConnectorContentList(List<ConnectorContent> contents)
	{
		final List<Object[]> retResults = new ArrayList<Object[]>();
		for( ConnectorContent content : contents )
		{
			final ConnectorContentAttribute instructor = content.getAttribute(ConnectorContent.KEY_INSTRUCTOR);
			final ConnectorContentAttribute dateAccessed = content.getAttribute(ConnectorContent.KEY_DATE_ACCESSED);
			final ConnectorContentAttribute enrollments = content.getAttribute(ConnectorContent.KEY_ENROLLMENTS);
			final String uuid = content.getUuid();
			final int version = content.getVersion();
			final String attachmentUuid = content.getAttachmentUuid();
			final String attachmentPath = content.getAttachmentUrl();

			// FIXME: shouldn't NEED an info. this is just DODGE-O-RAMA
			final SectionInfo info = sectionsController.createForward("/viewitem/viewitem.do");
			final ViewableItem<Item> viewableItem = viewableItemFactory
				.createNewViewableItem(new ItemId(uuid, version));
			String equellaUrl = null;
			String fileType = "";
			int fileSize = 0;

			if( !Check.isEmpty(attachmentUuid) )
			{
				try
				{
					IAttachment attach = viewableItem.getAttachmentByUuid(attachmentUuid);
					ViewableResource res = attachmentResoureService.getViewableResource(info, viewableItem, attach);
					fileType = res.getMimeType();
					if( res.hasContentStream() )
					{
						fileSize = (int) res.getContentStream().getContentLength();
					}
					ViewItemUrl viewerUrl = res.createDefaultViewerUrl();
					viewerUrl.addFlag(ViewItemUrl.FLAG_FULL_URL);
					equellaUrl = viewerUrl.getHref();
				}
				catch( AttachmentNotFoundException a )
				{
					// empty
				}
			}
			else if( !Check.isEmpty(attachmentPath) )
			{
				ViewableResource res = attachmentResoureService.createPathResource(info, viewableItem, attachmentPath,
					null);
				fileType = res.getMimeType();
				if( res.hasContentStream() )
				{
					fileSize = (int) res.getContentStream().getContentLength();
				}
				ViewItemUrl viewerUrl = res.createDefaultViewerUrl();
				viewerUrl.addFlag(ViewItemUrl.FLAG_FULL_URL);
				equellaUrl = viewerUrl.getHref();
			}

			//@formatter:off
			retResults.add(new Object[]{
				uuid, 
				version,
				attachmentUuid, 
				attachmentPath, 
				equellaUrl,
				content.getExternalTitle(),
				content.getFolderUrl(),
				content.getCourse(), 
				content.getCourseCode(),
				content.getFolder(), 
				convertDate(content.getDateAdded()),
				convertDate(content.getDateModified()),
				fileType,
				fileSize,
				content.isAvailable(),
				//different connectors may or may not return this data
				instructor == null ? null : instructor.getValue(),
				dateAccessed == null ? null : convertDate((Date)dateAccessed.getValue()),
				enrollments == null ? 0 : enrollments.getValue()
			});
			//@formatter:on
		}

		return new SimpleResultSet(retResults, getResultsMetadataBean());
	}

	private IResultSetExt fakeData()
	{
		final List<Object[]> retResults = new ArrayList<Object[]>();
		Integer zero = 0;
		retResults.add(new Object[]{"uuid", zero, "attachmentUuid", "attachmentPath", "equellaUrl", "externalTitle",
				"externalUrl", "course", "courseCode", "folder", null, null, "fileType", zero, Boolean.TRUE,
				// different connectors may or may not return this data
				"instructor", null, zero});

		return new SimpleResultSet(retResults, getResultsMetadataBean());
	}

	private Timestamp convertDate(Date date)
	{
		if( date != null )
		{
			return new Timestamp(date.getTime());
		}
		return null;
	}
}
