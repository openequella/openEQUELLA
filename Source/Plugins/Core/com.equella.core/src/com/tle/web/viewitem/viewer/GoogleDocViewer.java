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

package com.tle.web.viewitem.viewer;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.google.common.base.Strings;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * A file viewing utility to generate a URL which invokes google's doc viewer to
 * render various mime types of the MS office suite and other proprietary mime
 * types:
 * <ul>
 * <li>Microsoft Word (.DOC and .DOCX)</li>
 * <li>Microsoft Excel (.XLS and .XLSX)</li>
 * <li>Microsoft PowerPoint 2007 / 2010 (.PPTX)</li>
 * <li>Apple Pages (.PAGES)</li>
 * <li>Adobe Illustrator (.AI)</li>
 * <li>Adobe Photoshop (.PSD)</li>
 * <li>Autodesk AutoCad (.DXF)</li>
 * <li>Scalable Vector Graphics (.SVG)</li>
 * <li>PostScript (.EPS, .PS)</li>
 * <li>TrueType (.TTF)</li>
 * <li>XML Paper Specification (.XPS)</li>
 * <li>... and more!</li>
 * </ul>
 * 
 * @author larry
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class GoogleDocViewer extends FileViewer implements ViewItemViewer
{
	/**
	 * The string which acts as a link between specified mime types and the
	 * GoogleDoc action (in attributes.conf), and also serves as the key for the
	 * dialogue title.
	 */
	public static final String MIME_ACTION_KEY = "googledocviewer";

	/**
	 * There probably wont be any locale-specific alternatives to
	 * docs.google.com, but it makes more sense to preserve the URL In a
	 * properties file than hard-coded into java. Expected to evaluate to<br />
	 * <i>http://docs.google.com/viewer?url=</i>
	 */
	private static final String GOOGLE_DOC_URL_KEY = "googledoc.url";

	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(GoogleDocViewer.class);

	@Inject
	private UserService userService;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Override
	public String getViewerId()
	{
		return MIME_ACTION_KEY;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		GoogleDocViewerConfigDialog gdvcd = componentFactory.createComponent(parentId, "dfcd", tree,
			GoogleDocViewerConfigDialog.class, true);
		gdvcd.setTemplate(dialogTemplate);
		return gdvcd;
	}

	/**
	 * Gets the canonical string for the Equella resource. Converts that string
	 * to its URL encoded variant, so "http://mumble.com" becomes
	 * "http%3A%2F%2Fmumble.com" Prefix with the Google URL and package into a
	 * Bookmark we can return.
	 */

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		// the plain URL for the resource we are sending to Google
		String canonical = resource.createCanonicalUrl().getHref();

		return new SimpleBookmark(getViewerUrl(canonical));
	}

	private String getViewerUrl(String localFileUrl)
	{
		// we always addToken for a google doc view (ie without asking in the
		// dialogue)
		String token = userService.getGeneratedToken(Constants.APPLET_SECRET_ID, CurrentUser.getUsername());

		// URL-encode the token: the resultant encoded string will be a
		// substring
		// of a composite which then URL-encoded in its entirety (so the encoded
		// token
		// value gets again URL-encoded).
		UrlEncodedString encodedToken = UrlEncodedString.createFromValue(token);

		// Append either a "?" or a "&" (depends if POST parameter(s) already
		// exist) to the plain URL, with "token=<the encoded token>" into a
		// single string ...
		String fullEquellaURLWithTokenParam = localFileUrl + (localFileUrl.contains("?") ? "&" : "?") + "token="
			+ encodedToken;

		// ... all of which is then encoded as a suffix to the plain google URL)
		UrlEncodedString urlAllEncodedStringPlusToken = UrlEncodedString.createFromValue(fullEquellaURLWithTokenParam);

		// Get the prime-mover URL, the google document converter site.
		// Expect http://docs.google.com/viewer?url=
		String docsGoogleRsrcURL = CurrentLocale.get(RESOURCES.key(GOOGLE_DOC_URL_KEY));

		return docsGoogleRsrcURL + urlAllEncodedStringPlusToken.toString();
	}

	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		return this;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		//FIXME: this should render a redirect...
		if( !Strings.isNullOrEmpty(resource.getFilepath()) )
		{
			ViewableItem<?> vitem = resource.getViewableItem();
			ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, vitem,
				new UnmodifiableAttachments(vitem.getItem()).getAttachmentByFilename(resource.getFilepath()));

			info.forwardToUrl(getViewerUrl(viewableResource.createCanonicalUrl().getHref()));
		}
		else
		{
			info.setRendered();
			contentStreamWriter.outputStream(info.getRequest(), info.getResponse(),
				new AttachmentContentStream(resource.getContentStream()));
		}
		return null;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return true;
	}
}
