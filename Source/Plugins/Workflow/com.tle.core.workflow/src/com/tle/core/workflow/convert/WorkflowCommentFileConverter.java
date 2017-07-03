package com.tle.core.workflow.convert;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.item.Item;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.item.convert.ItemConverter.ItemExtrasConverter;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.service.WorkflowService;

@Bind
@Singleton
public class WorkflowCommentFileConverter implements ItemExtrasConverter
{
	private static final String FOLDER_NAME = "workflow";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private WorkflowService workflowService;

	@Override
	public void importExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder) throws IOException
	{
		final SubTemporaryFile workflowFolder = new SubTemporaryFile(extrasFolder, FOLDER_NAME);
		final Item item = info.getItem();
		final ModerationStatus moderation = item.getModeration();
		if( moderation != null )
		{
			final Set<WorkflowNodeStatus> statuses = moderation.getStatuses();
			for( WorkflowNodeStatus status : statuses )
			{
				final Set<WorkflowMessage> comments = status.getComments();
				for( WorkflowMessage message : comments )
				{
					final String uuid = message.getUuid();
					final SubTemporaryFile messageDir = new SubTemporaryFile(workflowFolder, uuid);
					fileSystemService.commitFiles(messageDir, new WorkflowMessageFile(uuid));
				}
			}
		}
	}

	@Override
	public void exportExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder) throws IOException
	{
		final ConverterParams params = info.getParams();
		final boolean attachments = !params.hasFlag(ConverterParams.NO_ITEMSATTACHMENTS);
		if( attachments )
		{
			final Item item = info.getItem();
			final List<WorkflowMessage> messages = workflowService.getMessages(item.getItemId());
			final SubTemporaryFile workflowDir = new SubTemporaryFile(extrasFolder, FOLDER_NAME);
			for( WorkflowMessage msg : messages )
			{
				final String uuid = msg.getUuid();
				final WorkflowMessageFile wfFile = new WorkflowMessageFile(uuid);
				final SubTemporaryFile messageDir = new SubTemporaryFile(workflowDir, uuid);
				fileSystemService.copyToStaging(wfFile, messageDir, false);
			}
		}
	}
}