package com.tle.web.kaltura.viewer;

import java.text.MessageFormat;
import java.util.UUID;

import javax.inject.Inject;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.web.searching.VideoPreviewRenderer;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.swfobject.SwfObject;
import com.tle.web.viewable.ViewableItem;

/**
 * @author Peng
 */
@Bind
public class KalturaPreviewRenderer implements VideoPreviewRenderer
{
	@PlugURL("js/kalturaviewerembed.js")
	private static String KALTURA_VIEWER;

	@Inject
	private KalturaService kalturaService;

	@Override
	public void preRender(PreRenderContext context)
	{
		context.preRender(SwfObject.PRERENDER);
		context.preRender(new IncludeFile(KALTURA_VIEWER));
	}

	@Override
	public SectionRenderable renderPreview(RenderContext context, Attachment attachment, ViewableItem<?> vitem,
		String mimeType)
	{
		if( supports(mimeType) )
		{
			String entryId = (String) attachment.getData(KalturaUtils.PROPERTY_ENTRY_ID);
			String uuid = (String) attachment.getData(KalturaUtils.PROPERTY_KALTURA_SERVER);

			if( !Check.isEmpty(entryId) && !Check.isEmpty(uuid) )
			{
				KalturaServer ks = kalturaService.getByUuid(uuid);
				String kdpUiConfId = getKdpUiConfId(ks, attachment);

				ObjectExpression kdpVars = new ObjectExpression();
				kdpVars.put("flashVersion", "9.0.0");
				kdpVars.put("width", 320);
				kdpVars.put("height", 180);
				kdpVars.put("embedUrl", createFlashEmbed(ks, kdpUiConfId, entryId));

				String kalturaPlayerId = "kalturaplayer" + UUID.randomUUID();
				TagState state = new HtmlComponentState();
				state.addReadyStatements(Js.statement(Js.call(new ExternallyDefinedFunction("setupKDP",
					SwfObject.PRERENDER, new IncludeFile(createHtml5embed(ks, kdpUiConfId)), new IncludeFile(
						KALTURA_VIEWER)), kalturaPlayerId, kdpVars)));

				TagRenderer tag = new TagRenderer("div", state);
				tag.setId(kalturaPlayerId);

				return tag;
			}
		}

		return null;
	}

	private String createHtml5embed(KalturaServer ks, String kdpUiConfId)
	{
		return MessageFormat.format("{0}/p/{1}/embedIframeJs/uiconf_id/{2}/partner_id/{1}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), kdpUiConfId);
	}

	private String createFlashEmbed(KalturaServer ks, String kdpUiConfId, String entryId)
	{
		return MessageFormat.format("{0}/kwidget/wid/_{1}/uiconf_id/{2}/entry_id/{3}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), kdpUiConfId, entryId);
	}

	private String getKdpUiConfId(KalturaServer ks, IAttachment a)
	{
		// // Attachment custom
		String uiConfId = (String) a.getData(KalturaUtils.PROPERTY_CUSTOM_PLAYER);

		if( !Check.isEmpty(uiConfId) && kalturaService.hasConf(ks, uiConfId) )
		{
			return uiConfId;
		}

		// Server default
		uiConfId = Integer.toString(ks.getKdpUiConfId());
		if( !Check.isEmpty(uiConfId) && kalturaService.hasConf(ks, uiConfId) )
		{
			return uiConfId;
		}

		// EQUELLA default
		uiConfId = Integer.toString(kalturaService.getDefaultKdpUiConf(ks).id);
		return uiConfId;
	}

	@Override
	public boolean supports(String mimeType)
	{
		if( mimeType.contains("equella/attachment-kaltura") )
		{
			return true;
		}
		return false;
	}
}
