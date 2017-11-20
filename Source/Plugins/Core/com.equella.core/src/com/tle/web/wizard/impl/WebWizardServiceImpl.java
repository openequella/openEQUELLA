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

package com.tle.web.wizard.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardConstants;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WebWizardServiceImpl.WizardEntryUrl.EntryMethod;
import com.tle.web.wizard.section.RootWizardSection;
import com.tle.web.wizard.section.model.WizardForm;
import com.tle.web.workflow.tasks.ModerationView;

/**
 * @author aholland
 */
@Bind(WebWizardService.class)
@Singleton
public class WebWizardServiceImpl implements WebWizardService, ModerationView
{
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private WizardService wizardService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemDefinitionService collectionService;

	@Override
	public void forwardToViewItem(SectionInfo info, WizardState state)
	{
		RootWizardSection rootSection = info.lookupSection(RootWizardSection.class);
		rootSection.removeState(info, state);

		ViewItemUrl vurl = urlFactory.createItemUrl(info, state.getItemId());
		vurl.forward(info);
	}

	@Override
	public void forwardToLoadItemWizard(SectionInfo info, String itemUuid, int itemVersion, boolean edit,
		boolean redraft, boolean newVersion)
	{
		WizardEntryUrl wizUrl = new WizardEntryUrl(EntryMethod.LOAD_ITEM);
		wizUrl.setUuid(itemUuid);
		wizUrl.setVersion(itemVersion);
		wizUrl.setEdit(edit);
		wizUrl.setRedraft(redraft);
		wizUrl.setNewversion(newVersion);
		wizUrl.forward(info);
	}

	@SuppressWarnings("nls")
	@Override
	public SectionInfo getNewItemWizardForward(SectionInfo info, String collectionUuid, PropBagEx initialXml,
		StagingFile staging, boolean cancellable)
	{
		try
		{
			final ItemDefinition collection = collectionService.getByUuid(collectionUuid);
			if( collection == null )
			{
				throw new RuntimeException("Collection with uuid '" + collectionUuid + "' doesn't exist");
			}
			PropBagEx bagXml = initialXml;
			if( bagXml == null )
			{
				final SelectionSession session = selectionService.getCurrentSession(info);
				if( session != null )
				{
					final String xml = session.getInitialItemXml();
					if( !Check.isEmpty(xml) )
					{
						bagXml = new PropBagEx(xml);
					}
				}
			}

			if( bagXml != null )
			{
				final WizardState state = wizardService.newItem(collection.getUuid(), bagXml, staging);
				wizardService.addToSession(info, state, true);

				final WizardEntryUrl url = new WizardEntryUrl(EntryMethod.LOAD_STATE);
				url.setWizardId(state.getWizid());
				url.setNoCancel(!cancellable);
				return url.getNewInfo(info);
			}

			final WizardEntryUrl entry = new WizardEntryUrl(EntryMethod.NEW_ITEM);
			entry.setCollectionUuid(collection.getUuid());
			entry.setNoCancel(!cancellable);
			return entry.getNewInfo(info);
		}
		catch( Exception e )
		{
			SectionUtils.throwRuntime(e);
			return null;
		}
	}

	@Override
	public void forwardToNewItemWizard(SectionInfo info, String collectionUuid, PropBagEx initialXml,
		StagingFile staging, boolean cancellable)
	{
		info.forwardAsBookmark(getNewItemWizardForward(info, collectionUuid, initialXml, staging, cancellable));
	}

	@Override
	public void forwardToCloneItemWizard(SectionInfo info, String newCollectionUuid, String itemUuid, int itemVersion,
		String transform, boolean move, boolean cloneAttachments)
	{
		final WizardEntryUrl entry = new WizardEntryUrl(EntryMethod.CLONE_ITEM);
		entry.setCollectionUuid(newCollectionUuid);
		entry.setUuid(itemUuid);
		entry.setVersion(itemVersion);
		entry.setImportTransform(transform);
		entry.setEdit(move);
		entry.setCloneAttachments(cloneAttachments);
		entry.forward(info);
	}

	@Override
	public void forwardToLoadWizard(SectionInfo info, String wizardUuid)
	{
		final WizardEntryUrl url = new WizardEntryUrl(EntryMethod.LOAD_STATE);
		url.setWizardId(wizardUuid);
		// url.setNoCancel(!cancellable);
		SectionInfo fwd = url.getNewInfo(info);
		info.forwardAsBookmark(fwd);
	}

	protected static class WizardEntryUrl
	{
		@SuppressWarnings("nls")
		protected enum EntryMethod
		{
			LOAD_ITEM()
			{
				@Override
				public String toString()
				{
					return "loaditem";
				}
			},
			LOAD_STATE()
			{
				@Override
				public String toString()
				{
					return "";
				}
			},
			NEW_ITEM()
			{
				@Override
				public String toString()
				{
					return "newitem";
				}
			},
			CLONE_ITEM()
			{
				@Override
				public String toString()
				{
					return "cloneitem";
				}
			}
		}

		private final EntryMethod method;

		private boolean redraft;
		private boolean edit;
		private boolean newversion;
		private String uuid;
		private int version;
		private String collectionUuid;
		private String wizardId;
		private String importTransform;
		private boolean cloneAttachments;
		private boolean noCancel;

		public WizardEntryUrl(final EntryMethod method)
		{
			this.method = method;
		}

		public Bookmark getBookmark(final SectionInfo info)
		{
			final SectionInfo newInfo = getNewInfo(info);
			return new InfoBookmark(newInfo, new BookmarkEvent("wizardEntry")); //$NON-NLS-1$
		}

		public SectionInfo getNewInfo(final SectionInfo info)
		{
			final SectionInfo newInfo = createForwardInfo(info);

			final WizardForm form = newInfo.getModelForId(""); //$NON-NLS-1$
			form.setMethod(method.toString());
			form.setItemdefUuid(collectionUuid);
			form.setWizid(wizardId);
			form.setUuid(uuid);
			form.setVersion(version);
			form.setRedraft(redraft);
			form.setEdit(edit);
			form.setNewversion(newversion);
			form.setTransform(importTransform);
			form.setCloneAttachments(cloneAttachments);
			form.setNoCancel(noCancel);

			return newInfo;
		}

		public void forward(final SectionInfo info)
		{
			final SectionInfo newInfo = getNewInfo(info);
			info.forwardAsBookmark(newInfo);
		}

		public WizardEntryUrl setEdit(final boolean edit)
		{
			this.edit = edit;
			return this;
		}

		public WizardEntryUrl setNewversion(final boolean newversion)
		{
			this.newversion = newversion;
			return this;
		}

		public WizardEntryUrl setRedraft(final boolean redraft)
		{
			this.redraft = redraft;
			return this;
		}

		public WizardEntryUrl setItem(final Item item)
		{
			this.uuid = item.getUuid();
			this.version = item.getVersion();
			return this;
		}

		public WizardEntryUrl setCollectionUuid(final String collectionUuid)
		{
			this.collectionUuid = collectionUuid;
			return this;
		}

		public WizardEntryUrl setUuid(final String uuid)
		{
			this.uuid = uuid;
			return this;
		}

		public WizardEntryUrl setVersion(final int version)
		{
			this.version = version;
			return this;
		}

		public WizardEntryUrl setWizardId(final String wizardId)
		{
			this.wizardId = wizardId;
			return this;
		}

		public WizardEntryUrl setImportTransform(final String importTransform)
		{
			this.importTransform = importTransform;
			return this;
		}

		public WizardEntryUrl setCloneAttachments(final boolean cloneAttachments)
		{
			this.cloneAttachments = cloneAttachments;
			return this;
		}

		public WizardEntryUrl setNoCancel(final boolean noCancel)
		{
			this.noCancel = noCancel;
			return this;
		}

		public SectionInfo createForwardInfo(final SectionInfo info)
		{
			return info.createForward(WizardConstants.WIZARD_ENTRY_URL);
		}
	}

	@Override
	public SectionInfo getViewForward(SectionInfo info, ItemTaskId itemTaskId, String view)
	{
		WizardEntryUrl wizUrl = new WizardEntryUrl(EntryMethod.LOAD_ITEM);
		wizUrl.setUuid(itemTaskId.getUuid());
		wizUrl.setVersion(itemTaskId.getVersion());
		return wizUrl.getNewInfo(info);
	}
}
