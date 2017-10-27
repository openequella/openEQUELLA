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

package com.tle.core.item.standard.operations;

import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.item.standard.service.MetadataMappingService;

/**
 * @author jmaginnis
 */
public class MetadataMapOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private transient MetadataMappingService metadataService;

	@Override
	public boolean execute()
	{
		try
		{
			StagingFile staging = getStaging();
			ItemDefinition collection = getCollection();
			PropBagEx itemxml = getItemXml();
			if( staging != null )
			{
				Attachments attachments = getAttachments();
				ImsAttachment packageAttach = attachments.getIms();
				if( packageAttach != null )
				{
					metadataService.mapPackage(collection, staging, packageAttach.getUrl(), itemxml);
				}
				else
				{
					List<CustomAttachment> scormAttachments = attachments.getCustomList("scorm"); //$NON-NLS-1$
					for( CustomAttachment attachment : scormAttachments )
					{
						metadataService.mapPackage(collection, staging, attachment.getUrl(), itemxml);
						break; // TODO only do one for now as multiple aren't
								// really supported
					}
				}
			}
			metadataService.mapLiterals(collection, itemxml, createScriptContext(null));
		}
		catch( Exception e )
		{
			throw new WorkflowException("Error mapping metadata", e); //$NON-NLS-1$
		}
		return true;
	}
}
