package com.example.webapp.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.example.service.JsonMapper;

public abstract class ModelUtils
{
	/**
	 * Transforms the "attachments" JSON node into a list of ViewAttachmentModel
	 * 
	 * @param resourceNode A JSON resource
	 * @param mapper The JsonMapper
	 * @return An empty list if no "attachments" node exists in the JSON,
	 *         otherwise a list of populated ViewAttachmentModel
	 */
	public static List<ViewAttachmentModel> convertToAttachmentModels(ObjectNode resourceNode)
	{
		final List<ViewAttachmentModel> attachments = new ArrayList<ViewAttachmentModel>();
		final JsonNode attachmentsNode = resourceNode.get("attachments");
		if( attachmentsNode != null )
		{
			for( JsonNode attachmentNode : attachmentsNode )
			{
				final ViewAttachmentModel attachmentModel = new ViewAttachmentModel();
				final String attachmentUuid = JsonMapper.getString(attachmentNode, "uuid", null);
				attachmentModel.setFilename(JsonMapper.getString(attachmentNode, "filename", null));
				attachmentModel.setDescription(JsonMapper.getString(attachmentNode, "description", attachmentUuid));
				attachmentModel.setSize(JsonMapper.getInt(attachmentNode, "size", 0));
				final JsonNode linksNode = attachmentNode.get("links");
				if( linksNode != null )
				{
					attachmentModel.setViewUrl(JsonMapper.getString(linksNode, "view", null));
					attachmentModel.setThumbUrl(JsonMapper.getString(linksNode, "thumbnail", null));
				}
				attachments.add(attachmentModel);
			}
		}
		return attachments;
	}
}
