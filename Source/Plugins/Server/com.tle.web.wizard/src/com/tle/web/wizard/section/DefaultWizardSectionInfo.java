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

package com.tle.web.wizard.section;

import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.SecurityStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;

@Bind
public class DefaultWizardSectionInfo implements WizardSectionInfo
{
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private WizardService wizardService;

	private WizardState wizardState;
	private Attachments attachments;
	private ItemDefinition collection;

	@Override
	public ItemDefinition getItemdef()
	{
		if( collection == null )
		{
			collection = itemDefinitionService.get(getItem().getItemDefinition().getId());
		}
		return collection;
	}

	public DefaultWizardSectionInfo()
	{
		super();
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return wizardService.getWorkflowStatus(wizardState);
	}

	@Override
	public PropBagEx getItemxml()
	{
		return wizardState.getItemxml();
	}

	@Override
	public WizardState getWizardState()
	{
		return wizardState;
	}

	public void setWizardState(WizardState wizardState)
	{
		this.wizardState = wizardState;
	}

	@Override
	public Item getItem()
	{
		return wizardState.getItem();
	}

	@Override
	public String getItemdir()
	{
		return getViewableItem().getItemdir();
	}

	@Override
	public ItemKey getItemId()
	{
		return wizardState.getItemId();
	}

	@Override
	public Set<String> getPrivileges()
	{
		return getSecurityStatus().getAllowedPrivileges();
	}

	@Override
	public boolean hasPrivilege(String privilege)
	{
		return getPrivileges().contains(privilege);
	}

	public void update(ItemPack pack, WorkflowStatus status)
	{
		wizardState.update(pack, status);
	}

	public boolean isUpdated()
	{
		throw new RuntimeException("Should never be called"); //$NON-NLS-1$
	}

	public void resetUpdated()
	{
		throw new RuntimeException("Should never be called"); //$NON-NLS-1$
	}

	public SecurityStatus getSecurityStatus()
	{
		return getWorkflowStatus().getSecurityStatus();
	}

	public ViewableItemType getViewableItemType()
	{
		return wizardState.getItemType();
	}

	@Override
	public ViewableItem getViewableItem()
	{
		return wizardService.createViewableItem(wizardState);
	}

	@Override
	public void modify(WorkflowOperation... ops)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEditing()
	{
		return wizardState.isLockedForEditing();
	}

	@Override
	public void refreshItem(boolean modified)
	{
		try
		{
			wizardService.reload(wizardState, false);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Attachments getAttachments()
	{
		if( attachments == null )
		{
			attachments = new UnmodifiableAttachments(getItem());
		}
		return attachments;
	}

	@Override
	public void cancelEdit()
	{
		wizardService.cancelEdit(wizardState);
	}

	@Override
	public boolean isAvailableForEditing()
	{
		return !wizardService.getWorkflowStatus(wizardState).isLocked();
	}

	@Override
	public boolean isLockedForEditing()
	{
		return wizardState.isLockedForEditing();
	}

	@Override
	public boolean isNewItem()
	{
		return wizardState.isNewItem();
	}
}
