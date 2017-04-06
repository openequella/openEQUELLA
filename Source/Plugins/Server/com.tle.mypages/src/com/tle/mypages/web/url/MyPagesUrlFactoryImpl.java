package com.tle.mypages.web.url;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ModalSessionService;
import com.tle.web.sections.equella.ParentFrameCallback;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author aholland
 */
@Bind(MyPagesUrlFactory.class)
@Singleton
public class MyPagesUrlFactoryImpl implements MyPagesUrlFactory
{
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;
	@Inject
	private ModalSessionService modalSessionService;

	@Override
	public MyPagesEditUrl createEditUrl(SectionInfo info, String wizid, HtmlAttachment page,
		JSCallAndReference finishedCallback)
	{
		return new MyPagesEditUrl(info, wizid, (page == null ? Constants.BLANK : page.getUuid()),
			new ParentFrameCallback(finishedCallback), modalSessionService);
	}

	@Override
	public ViewItemUrl createViewUrl(SectionInfo info, ViewableItem vitem)
	{
		// oh yuk, a special jsp file path... this makes baby Jesus cry.
		ViewItemUrl viewItemUrl = viewItemUrlFactory.createItemUrl(info, vitem.getItemId(),
			UrlEncodedString.createFromFilePath("treenav.jsp")); //$NON-NLS-1$
		viewItemUrl.addFlag(ViewItemUrl.FLAG_NO_SELECTION);
		return viewItemUrl;
	}

	@Override
	public ViewItemUrl createViewUrl(SectionInfo info, ViewableItem vitem, HtmlAttachment page)
	{
		ViewableResource resource = attachmentResourceService.getViewableResource(info, vitem, page);
		return resource.createDefaultViewerUrl();
	}
}
