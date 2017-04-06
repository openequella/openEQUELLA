package com.tle.web.pss.viewer;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.pss.service.PearsonScormServicesService;
import com.tle.core.pss.util.PSSConstants;
import com.tle.core.services.UrlService;
import com.tle.web.pss.viewer.PearsonScormServicesViewerSection.ScormViewerModel;
import com.tle.web.scorm.ScormUtils;
import com.tle.web.scorm.treeviewer.ScormTreeNavigationSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.SimpleFormAction;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.AbstractAttachmentViewItemResource;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind
public class PearsonScormServicesViewerSection extends AbstractViewerSection<ScormViewerModel>
	implements
		ViewItemFilter
{
	@Inject
	private PearsonScormServicesService pssService;
	@Inject
	private UrlService urlService;
	@Inject
	private MimeTypeService mimeTypeService;

	@PlugKey("viewer.error")
	private static String VIEWER_ERROR;
	@PlugKey("viewer.pss.disabled")
	private static String VIEWER_PSS_DISABLED;

	@Inject
	private AttachmentResourceService attachmentResourceService;

	@TreeLookup
	private RootItemFileSection rootSection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addFilterViewer(this);
	}

	@Override
	public int getOrder()
	{
		return 0;
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		if( ScormTreeNavigationSection.VIEWSCORM_JSP.equals(resource.getFilepath()) && resource.getViewer() == null
			&& PSSConstants.PSS_VIEWER_ID.equals(resource.getDefaultViewerId()) )
		{
			CustomAttachment scormAtt = new UnmodifiableAttachments(resource.getViewableItem().getItem())
				.getFirstCustomOfType(ScormUtils.ATTACHMENT_TYPE);

			if( scormAtt != null )
			{
				ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
					resource.getViewableItem(), scormAtt);
				return new PSSAttachmentViewItemResource(resource, viewableResource, false);
			}
		}

		return resource;
	}

	/**
	 * A request for a launch of a playable SCO is to POST an LTI request.
	 */
	@SuppressWarnings("nls")
	@Override
	public SectionResult view(RenderContext context, ViewItemResource resource)
	{
		Decorations.getDecorations(context).clearAllDecorations();

		if( !pssService.isEnabled() )
		{
			throw new RuntimeApplicationException(CurrentLocale.get(VIEWER_PSS_DISABLED));
		}

		ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		if( viewableResource == null )
		{
			throw new RuntimeException("No viewable resource attribute");
		}
		IAttachment attachment = viewableResource.getAttachment();
		if( attachment == null )
		{
			throw new RuntimeException("No attachment on viewable resource");
		}

		Decorations.getDecorations(context).setTitle(new TextLabel(attachment.getDescription()));
		ResourceViewerConfig config = getResourceViewerConfig(mimeTypeService, resource, PSSConstants.PSS_VIEWER_ID);

		try
		{
			IItem<?> item = resource.getViewableItem().getItem();

			String pssReturnUrl = urlService.institutionalise(PathUtils.urlPath("pss/pss_return_url", item.getUuid(),
				Integer.toString(item.getVersion())));

			Map<String, String> composedFormParams = pssService.pseudoLaunchScormPackage(context.getRequest(), item,
				attachment, pssReturnUrl, config);
			FormTag formTag = context.getForm();
			formTag.setName("ltiLaunchForm");
			formTag.setElementId(new SimpleElementId("ltiLaunchForm"));
			formTag.setAction(new SimpleFormAction(pssService.getLaunchURL()));
			formTag.setEncoding("application/x-www-form-urlencoded");
			formTag.setMethod("POST");
			for( String key : composedFormParams.keySet() )
			{
				String val = composedFormParams.get(key);
				if( !Check.isEmpty(val) )
				{
					formTag.addHidden(new HiddenInput(key, val));
				}
			}
			// a neat way of calling "submit"
			formTag.addReadyStatements(Js.statement(Js.methodCall(Jq.$('#' + formTag.getElementId(context)),
				Js.function("submit"))));

		}
		catch( Exception e )
		{
			getModel(context).setError(new KeyLabel(VIEWER_ERROR, e.getMessage()));
			return viewFactory.createResult("pss-error.ftl", this);
		}
		return null;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_LOW)
	public void ensurePOST(SectionInfo info)
	{
		if( pssService.isEnabled() )
		{
			final ViewItemResource viewItemResource = rootSection.getViewItemResource(info);
			if( viewItemResource != null && viewItemResource.getViewer() == this )
			{
				info.preventGET();
			}
		}
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public Class<ScormViewerModel> getModelClass()
	{
		return ScormViewerModel.class;
	}

	public static class ScormViewerModel
	{
		private Label error;

		public Label getError()
		{
			return error;
		}

		public void setError(Label error)
		{
			this.error = error;
		}
	}

	public class PSSAttachmentViewItemResource extends AbstractAttachmentViewItemResource
	{
		public PSSAttachmentViewItemResource(ViewItemResource inner, ViewableResource viewableResource,
			boolean forcedStream)
		{
			super(inner, viewableResource, forcedStream);
		}

		@Override
		public ViewItemViewer getViewer()
		{
			return forcedStream ? PearsonScormServicesViewerSection.this : null;
		}
	}
}
