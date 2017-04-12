package com.tle.web.echo.data;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AttachmentSerializer;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.echo.EchoUtils;

@Bind
@Singleton
public class EchoAttachmentSerializer implements AttachmentSerializer
{
	@Inject
	private ObjectMapperService objectMapperService;

	private ObjectMapper mapper;

	public synchronized ObjectMapper getMapper()
	{
		if( mapper == null )
		{
			mapper = objectMapperService.createObjectMapper("rest");
		}

		return mapper;
	}

	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment cattach = (CustomAttachment) attachment;
		EchoAttachmentBean ebean = new EchoAttachmentBean();
		EchoAttachmentData ed;
		try
		{
			ed = getMapper()
				.readValue((String) cattach.getData(EchoUtils.PROPERTY_ECHO_DATA), EchoAttachmentData.class);
			ebean.setEchoData(ed.getEchoData());
			ebean.setPresenters(ed.getPresenters());
			ebean.setDescription(cattach.getDescription());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		return ebean;
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		EchoAttachmentEditor editor = itemEditor.getAttachmentEditor(uuid, EchoAttachmentEditor.class);
		EchoAttachmentBean ebean = (EchoAttachmentBean) bean;

		EchoAttachmentData ed = new EchoAttachmentData(ebean.getEchoData(), ebean.getPresenters());
		try
		{
			String json = getMapper().writeValueAsString(ed);
			editor.editEchoData(json);
			editor.editDescription(ebean.getDescription());
			editor.editViewer(ebean.getViewer());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		return uuid;
	}

	@Override
	@SuppressWarnings("nls")
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("echo", EchoAttachmentBean.class);
		return builder.build();
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return false;
	}
}
