package com.tle.web.kaltura.viewer;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.swfobject.SwfObject;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewableResource;

@Bind
@SuppressWarnings("nls")
public class KalturaViewerSection extends AbstractViewerSection<Object>
{
	@PlugURL("js/kalturaviewerembed.js")
	private static String KALTURA_VIEWER;

	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private KalturaService kalturaService;

	@Component
	private Div kdpDiv;

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		Decorations.getDecorations(info).clearAllDecorations();

		String height = null;
		String width = null;

		ResourceViewerConfig config = getResourceViewerConfig(mimeTypeService, resource, "kalturaViewer");

		if( config != null )
		{
			Map<String, Object> attr = config.getAttr();
			height = (String) attr.get("kalturaHeight");
			width = (String) attr.get("kalturaWidth");
		}

		if( Check.isEmpty(width) || Objects.equals(width, "undefined") )
		{
			width = "100%";
		}

		if( Check.isEmpty(height) || Objects.equals(height, "undefined") )
		{
			height = "100%";
		}

		setupKalturaKdp(info, resource, width, height);

		return viewFactory.createTemplateResult("viewer/kalturaviewer.ftl", this);
	}

	private String createHtml5embed(KalturaServer ks, IAttachment a)
	{
		return MessageFormat.format("{0}/p/{1}/embedIframeJs/uiconf_id/{2}/partner_id/{1}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), getKdpUiConfId(ks, a));
	}

	private String createFlashEmbed(KalturaServer ks, IAttachment a, String entryId)
	{
		return MessageFormat.format("{0}/kwidget/wid/_{1}/uiconf_id/{2}/entry_id/{3}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), getKdpUiConfId(ks, a), entryId);
	}

	private String getKdpUiConfId(KalturaServer ks, IAttachment a)
	{
		// Attachment custom
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

	private KalturaServer getKalturaServer(String uuid)
	{
		return kalturaService.getByUuid(uuid);
	}

	private void setupKalturaKdp(SectionInfo info, ViewItemResource resource, String width, String height)
	{
		ObjectExpression kdpVars = new ObjectExpression();
		final IAttachment a = getAttachment(resource);

		String entryId = (String) a.getData(KalturaUtils.PROPERTY_ENTRY_ID);
		String uuid = (String) a.getData(KalturaUtils.PROPERTY_KALTURA_SERVER);

		KalturaServer ks = getKalturaServer(uuid);

		kdpVars.put("flashVersion", "9.0.0");
		kdpVars.put("width", width);
		kdpVars.put("height", height);
		kdpVars.put("embedUrl", createFlashEmbed(ks, a, entryId));

		kdpDiv.addReadyStatements(info, Js.statement(Js.call(new ExternallyDefinedFunction("setupKDP",
			SwfObject.PRERENDER, new IncludeFile(createHtml5embed(ks, a)), new IncludeFile(KALTURA_VIEWER)), kdpDiv
			.getElementId(info), kdpVars)));
	}

	private IAttachment getAttachment(ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		return viewableResource.getAttachment();
	}

	public Div getKdpDiv()
	{
		return kdpDiv;
	}
}
