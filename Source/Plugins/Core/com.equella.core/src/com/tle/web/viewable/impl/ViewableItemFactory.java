/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewable.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.sections.Bookmark;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.NewViewableItemState;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.FilestoreBookmark;

@Bind
@Singleton
public class ViewableItemFactory
{
	private static final String PREVIEW_CONTEXT = "preview/"; //$NON-NLS-1$
	@Inject
	private Provider<NewDefaultViewableItem> viewableItemProvider;

	@Inject
	private InstitutionService institutionService;

	public NewDefaultViewableItem createNewViewableItem(ItemKey itemId)
	{
		return createNewViewableItem(itemId, false);
	}

	public NewDefaultViewableItem createNewViewableItem(ItemKey itemId, boolean latest)
	{
		NewDefaultViewableItem nvi = viewableItemProvider.get();
		NewViewableItemState state = nvi.getState();
		state.setItemId(itemId);
		state.setLatest(latest);
		state.setRequireDRM(!(itemId instanceof ItemTaskId));
		return nvi;
	}

	public NewDefaultViewableItem createIntegrationViewableItem(ItemKey itemKey, ViewableItemType vtype, boolean latest)
	{
		NewDefaultViewableItem nvi = viewableItemProvider.get();
		NewViewableItemState state = nvi.getState();
		state.setLatest(latest);
		state.setContext(vtype.getContext() + '/');
		state.setItemId(itemKey);
		return nvi;
	}

	public URI getServletPathForPreview(String previewId)
	{
		try
		{
			return new URI(null, null, PREVIEW_CONTEXT + new ItemId(previewId, 1).toString() + '/', null);
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	public String getItemdirForPreview(String previewId)
	{
		String path = institutionService.getInstitutionUrl().getPath();
		return path + PREVIEW_CONTEXT + new ItemId(previewId, 1).toString() + '/';
	}

	public ViewableItem createPreviewItem(String previewId, String stagingId)
	{
		return new PathOnlyViewableItem(stagingId, getItemdirForPreview(previewId));
	}

	public class PathOnlyViewableItem implements ViewableItem
	{
		private final String itemdir;
		private final String stagingId;
		private boolean fromRequest;

		public PathOnlyViewableItem(String stagingId, String itemdir)
		{
			this.stagingId = stagingId;
			this.itemdir = itemdir;
		}

		@Override
		public URI getServletPath()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Attachment getAttachmentByFilepath(String filepath)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Attachment getAttachmentByUuid(String uuid)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public FileHandle getFileHandle()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Item getItem()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public ItemId getItemId()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getItemdir()
		{
			return itemdir;
		}

		@Override
		public PropBagEx getItemxml()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<String> getPrivileges()
		{
			return Collections.singleton("VIEW_ITEM"); //$NON-NLS-1$
		}

		@Override
		public WorkflowStatus getWorkflowStatus()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isDRMApplicable()
		{
			return false;
		}

		@Override
		public boolean isItemForReal()
		{
			return false;
		}

		@Override
		public void refresh()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void update(ItemPack pack, WorkflowStatus status)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bookmark createStableResourceUrl(String path)
		{
			return new FilestoreBookmark(institutionService, stagingId, path);
		}

		@Override
		public boolean isFromRequest()
		{
			return fromRequest;
		}

		@Override
		public void setFromRequest(boolean fromRequest)
		{
			this.fromRequest = fromRequest;
		}

		@Override
		public String getItemExtensionType()
		{
			return null;
		}
	}
}
