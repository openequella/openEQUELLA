/*
 * Created on Nov 25, 2004 For "The Learning Edge"
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
