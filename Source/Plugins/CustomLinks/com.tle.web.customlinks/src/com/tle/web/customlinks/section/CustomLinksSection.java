package com.tle.web.customlinks.section;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.dytech.common.GeneralConstants;
import com.dytech.edge.wizard.WizardTimeoutException;
import com.google.common.io.ByteStreams;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.util.ByteLimitedInputStream;
import com.tle.core.customlinks.service.CustomLinkEditingBean;
import com.tle.core.customlinks.service.CustomLinkEditingSession;
import com.tle.core.customlinks.service.CustomLinkService;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserService;
import com.tle.web.customlinks.CustomLinkContentHandler;
import com.tle.web.customlinks.CustomLinkListComponent;
import com.tle.web.customlinks.menu.CustomLinksMenuContributor;
import com.tle.web.customlinks.model.CustomLinksModel;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQuerySortable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

@NonNullByDefault
@SuppressWarnings("nls")
public class CustomLinksSection extends OneColumnLayout<CustomLinksModel>
{
	private static final String FILE_NAME_KEY = "fileName";

	private static final int MAX_BYTES = GeneralConstants.BYTES_PER_MEGABYTE;

	private static final JSCallable SETUP_LINKS = new ExternallyDefinedFunction("setupLinks", JQuerySortable.PRERENDER,
		new IncludeFile(ResourcesService.getResourceHelper(CustomLinksSection.class).url("js/setuplinks.js")));

	@PlugKey("edit.delete")
	private static Label DELETE_LABEL;
	@PlugKey("confirm.delete")
	private static Confirm DELETE_CONFIRM;
	@PlugKey("edit.edit")
	private static Label EDIT_LABEL;
	@PlugKey("page.customlinks.title")
	private static Label TITLE_LABEL;
	@PlugKey("page.breadcrumb.title")
	private static Label BREADCRUMB_TITLE_LABEL;
	@PlugKey("edit.select")
	private static Label EXPRESSION_SELECTOR_TITLE_LABEL;
	@PlugKey("error.name")
	private static String ERROR_NAME_KEY;
	@PlugKey("error.url")
	private static String ERROR_URL_KEY;
	@PlugKey("edit.add.title")
	private static Label EDIT_ADD_TITLE_LABEL;
	@PlugKey("edit.addText")
	private static Label EDIT_ADD_HEADING_LABEL;
	@PlugKey("edit.edit.title")
	private static Label EDIT_EDIT_TITLE_LABEL;
	@PlugKey("edit.editLink")
	private static Label EDIT_EDIT_HEADING_LABEL;
	@PlugKey("edit.existing")
	private static Label EDIT_EXISTING_HEADING_LABEL;

	@EventFactory
	private EventGenerator eventFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private CustomLinkService linkService;
	@Inject
	private CustomLinksMenuContributor menuContributor;
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ImageMagickService imageMagickService;
	@Inject
	private UrlService urlService;
	@Inject
	private TLEAclManager aclService;

	@Inject
	@Component(name = "dn", stateful = false)
	private MultiEditBox displayNameField;
	@Component(name = "uf", stateful = false)
	private TextField urlField;
	@Component(name = "nw", stateful = false)
	private Checkbox newWindow;

	@Component
	@PlugKey("edit.add")
	private Button addButton;
	@Component
	@PlugKey("edit.save")
	private Button saveButton;
	@Component
	@PlugKey("edit.cancel")
	private Button cancelButton;
	@Component
	@PlugKey("edit.upload.button")
	private Button uploadButton;
	@Component
	@PlugKey("edit.download.button")
	private Button downloadButton;
	@Component
	@PlugKey("edit.upload.delete")
	private Button deleteIconButton;
	@Component
	private FileUpload file;

	private ImageRenderer image;
	private DivRenderer linkDiv;

	private JSCallable deleteUrlFunc;
	private SubmitValuesFunction editUrlFunc;

	@Inject
	private ExpressionSelectorDialog selector;

	private SubmitValuesHandler cancelEditFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		editUrlFunc = eventFactory.getSubmitValuesFunction("editUrl");
		cancelEditFunc = eventFactory.getNamedHandler("cancelUrl");

		addButton.setClickHandler(eventFactory.getNamedHandler("addUrl"));
		saveButton.setClickHandler(eventFactory.getNamedHandler("saveUrl"));
		cancelButton.setClickHandler(eventFactory.getNamedHandler("cancelUrl"));

		selector.setTitle(EXPRESSION_SELECTOR_TITLE_LABEL);
		selector.setOkCallback(eventFactory.getSubmitValuesFunction("expression"));

		componentFactory.registerComponent(id, "selector", tree, selector);

		displayNameField.addReadyStatements(new JQueryStatement(displayNameField, new FunctionCallExpression("focus")));

		TagState tag = new TagState();
		tag.addReadyStatements(SETUP_LINKS, new ObjectExpression("movedCallback", ajax.getAjaxFunction("urlMoved")),
			"#cls_us");
		linkDiv = new DivRenderer(tag);

		uploadButton.setClickHandler(eventFactory.getNamedHandler("upload", false));
		deleteIconButton.setClickHandler(eventFactory.getNamedHandler("removeIcon"));
		downloadButton.setClickHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			eventFactory.getEventHandler("downloadFavicon"), "currentIcon", "downloadIcon")));
		deleteUrlFunc = ajax.getAjaxUpdateDomFunction(tree, this, eventFactory.getEventHandler("deleteUrl"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "linkListDiv");
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		CustomLinksModel model = getModel(context);

		Decorations decs = Decorations.getDecorations(context);
		decs.setContentBodyClass("customlinks");

		String expression = selector.getExpression(context);
		if( Check.isEmpty(expression) )
		{
			model.setExpressionPretty("");
		}
		else
		{
			model.setExpressionPretty(new ExpressionFormatter(userService).convertToInfix(expression));
		}
		selector.setExpression(context, expression);

		model.setLinks(createLinkList());

		GenericTemplateResult temp = new GenericTemplateResult();
		String sessionId = model.getSessionId();
		if( !Check.isEmpty(sessionId) )
		{
			CustomLinkEditingSession session = linkService.loadSession(sessionId);
			model.setErrors(session.getValidationErrors());

			final CustomLinkEditingBean bean = session.getBean();
			model.setEntityUuid(session.getBean().getUuid());
			if( bean.getId() == 0 )
			{
				model.setHeading(EDIT_ADD_HEADING_LABEL);
				decs.setTitle(EDIT_ADD_TITLE_LABEL);
			}
			else
			{
				model.setHeading(EDIT_EDIT_HEADING_LABEL);
				decs.setTitle(EDIT_EDIT_TITLE_LABEL);
			}

			HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark("access/customlinks.do"));
			linkState.setLabel(TITLE_LABEL);
			linkState.setTitle(BREADCRUMB_TITLE_LABEL);
			linkState.setClickHandler(cancelEditFunc);
			Breadcrumbs.get(context).add(linkState);

			image = new ImageRenderer(getIconUrl(context, bean.getUuid()), new TextLabel(bean.getFileName()));

			if( Check.isEmpty(urlField.getValue(context)) )
			{
				urlField.setValue(context, "http://");
			}
			temp.addNamedResult("body", viewFactory.createResult("edit.ftl", this));
		}
		else
		{
			decs.setTitle(TITLE_LABEL);
			model.setHeading(EDIT_EXISTING_HEADING_LABEL);
			addButton.setDisplayed(context, canAdd());
			temp.addNamedResult("body", viewFactory.createResult("list.ftl", this));
		}

		HelpAndScreenOptionsSection.addHelp(context, viewFactory.createResult("help.ftl", this));
		return temp;
	}

	private boolean canAdd()
	{
		return !aclService.filterNonGrantedPrivileges(Arrays.asList("CREATE_CUSTOM_LINK")).isEmpty();
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		// Varies depending on scenario. Done in renderHtml above.
	}

	@EventHandlerMethod
	public void addUrl(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		model.setEditing(true);
		CustomLinkEditingSession session = linkService.startNewSession(new CustomLink());
		loadInternal(info, session);
		model.setSessionId(session.getSessionId());
		selector.setExpression(info, null);
	}

	@EventHandlerMethod
	public void deleteUrl(SectionInfo info, String uuid)
	{
		CustomLink url = linkService.getByUuid(uuid);
		if( url != null )
		{
			String fileName = url.getAttribute(FILE_NAME_KEY);
			if( !Check.isEmpty(fileName) )
			{
				EntityFile entityFile = new EntityFile(url);
				fileSystemService.removeFile(entityFile, fileName);
			}
			linkService.deleteLink(url);
			clearCache();
		}
	}

	@EventHandlerMethod
	public void saveUrl(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		CustomLinkEditingSession session = linkService.loadSession(model.getSessionId());
		if( session.isValid() )
		{
			linkService.commitSession(session);
			clearCache();
			model.setEditing(false);
			model.setSessionId(null);
		}
	}

	@EventHandlerMethod
	public void cancelUrl(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		linkService.cancelSessionId(model.getSessionId());
		model.setEditing(false);
		model.setSessionId(null);
		selector.setExpression(info, null);
	}

	@EventHandlerMethod
	public void editUrl(SectionInfo info, String uuid)
	{
		CustomLink url = linkService.getByUuid(uuid);
		if( url != null )
		{
			CustomLinkEditingSession session = linkService.startEditingSession(url.getUuid());
			loadInternal(info, session);
			CustomLinksModel model = getModel(info);
			model.setSessionId(session.getSessionId());
			model.setEditing(true);
		}
	}

	@EventHandlerMethod
	public void expression(SectionInfo info, String selectorId, String expression)
	{
		CustomLinkEditingSession session = linkService.loadSession(getModel(info).getSessionId());
		session.getBean().setTargetExpression(expression);
		loadInternal(info, session);
	}

	@EventHandlerMethod
	public void downloadFavicon(SectionInfo info) throws IOException
	{
		CustomLinksModel model = getModel(info);
		CustomLinkEditingSession session = linkService.loadSession(model.getSessionId());
		final CustomLinkEditingBean bean = session.getBean();

		String url = urlField.getValue(info);
		URL fullUrl;
		GetMethod method;
		HttpClient client;

		try
		{
			fullUrl = parseHtmlForFav(url);
			method = new GetMethod(fullUrl.toString());
			method.setFollowRedirects(true);

			client = new HttpClient();
			client.executeMethod(method);
		}
		catch( UnknownHostException ex )
		{
			session.getValidationErrors().put("urlField",
				CurrentLocale.get("com.tle.web.customlinks.edit.download.error.host"));
			return;
		}
		catch( IllegalArgumentException ex )
		{
			session.getValidationErrors().put("urlField",
				CurrentLocale.get("com.tle.web.customlinks.edit.download.error.invalid", ex.getLocalizedMessage()));
			return;
		}
		catch( ConnectException ex )
		{
			session.getValidationErrors().put("urlField",
				CurrentLocale.get("com.tle.web.customlinks.edit.download.error.connect", ex.getLocalizedMessage()));
			return;
		}
		catch( Exception ex )
		{
			session.getValidationErrors().put("urlField", ex.toString());
			return;
		}

		if( method.getStatusCode() != HttpStatus.SC_OK )
		{
			session.getValidationErrors().put("urlField",
				CurrentLocale.get("com.tle.web.customlinks.edit.download.error.notfound"));
			return;
		}

		removeIcon(info);
		try (ByteLimitedInputStream remoteStream = new ByteLimitedInputStream(method.getResponseBodyAsStream(),
			MAX_BYTES))
		{
			StagingFile stagingFile = new StagingFile(session.getPack().getStagingID());
			String[] split = fullUrl.getPath().split("/");
			String filename = split[split.length - 1];
			String tempFilename = "TEMP-" + filename;

			fileSystemService.write(stagingFile, tempFilename, remoteStream, false);

			if( !imageMagickService.supported(mimeTypeService.getMimeTypeForFilename(filename)) )
			{
				session.getValidationErrors().put("urlField",
					CurrentLocale.get("com.tle.web.customlinks.edit.download.error.process"));
				return;
			}

			Dimension dimensions = imageMagickService.getImageDimensions(stagingFile, tempFilename);
			if( dimensions.getHeight() > 20 || dimensions.getWidth() > 20 )
			{
				File temp = fileSystemService.getExternalFile(stagingFile, tempFilename);
				File newFile = fileSystemService.getExternalFile(stagingFile, filename);
				imageMagickService.sample(temp, newFile, String.valueOf(20), String.valueOf(20));
			}
			else
			{
				fileSystemService.copy(stagingFile, tempFilename, filename);
			}
			fileSystemService.removeFile(stagingFile, tempFilename);

			bean.setFileName(filename);
			model.setFileName(filename);
		}
	}

	@EventHandlerMethod
	public void getIcon(SectionInfo info, String uuid, String filename) throws IOException
	{
		HttpServletResponse response = info.getResponse();
		response.setContentType(mimeTypeService.getMimeTypeForFilename(filename));

		StagingFile stagingFile = new StagingFile(uuid);
		if( fileSystemService.fileExists(stagingFile, filename) && !fileSystemService.fileIsDir(stagingFile, filename) )
		{
			try (InputStream in = fileSystemService.read(stagingFile, filename);
				ServletOutputStream out = response.getOutputStream())
			{
				ByteStreams.copy(in, out);
			}
			finally
			{
				info.setRendered();
			}
		}
	}

	@EventHandlerMethod
	public void removeIcon(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		CustomLinkEditingSession session = linkService.loadSession(model.getSessionId());
		final CustomLinkEditingBean bean = session.getBean();

		bean.setAttribute(FILE_NAME_KEY, null);
		StagingFile stagingFile = new StagingFile(session.getPack().getStagingID());
		String fileName = model.getFileName();
		if( fileSystemService.fileExists(stagingFile, fileName) )
		{
			fileSystemService.removeFile(stagingFile, fileName);
		}
		bean.setFileName(null);
	}

	@EventHandlerMethod
	public void upload(SectionContext context, boolean onSave) throws IOException
	{
		CustomLinksModel model = getModel(context);
		CustomLinkEditingSession session = linkService.loadSession(model.getSessionId());
		final CustomLinkEditingBean bean = session.getBean();

		if( Check.isEmpty(file.getFilename(context)) || file.getFileSize(context) <= 0 )
		{
			if( onSave )
			{
				session.getValidationErrors().remove("upload");
				return;
			}
			session.getValidationErrors().put("upload",
				CurrentLocale.get("com.tle.web.customlinks.edit.upload.error.empty"));
			return;
		}
		removeIcon(context);

		StagingFile stagingFile = new StagingFile(session.getPack().getStagingID());
		String filename = file.getFilename(context);
		String tempFilename = "TEMP-" + filename;
		try (InputStream stream = file.getInputStream(context))
		{
			fileSystemService.write(stagingFile, tempFilename, stream, false);

			if( !imageMagickService.supported(mimeTypeService.getMimeTypeForFilename(filename)) )
			{
				session.getValidationErrors().put("upload",
					CurrentLocale.get("com.tle.web.customlinks.edit.upload.error.unsupported"));
				return;
			}
			Dimension dimensions = imageMagickService.getImageDimensions(stagingFile, tempFilename);
			if( dimensions.getHeight() > 20 || dimensions.getWidth() > 20 )
			{
				File temp = fileSystemService.getExternalFile(stagingFile, tempFilename);
				File newFile = fileSystemService.getExternalFile(stagingFile, filename);
				imageMagickService.sample(temp, newFile, String.valueOf(20), String.valueOf(20));
			}
			else
			{
				fileSystemService.copy(stagingFile, tempFilename, filename);
			}
			fileSystemService.removeFile(stagingFile, tempFilename);
		}

		bean.setFileName(filename);
		session.getValidationErrors().remove("upload");
	}

	@AjaxMethod
	public String urlMoved(SectionInfo info, String uuid, int order)
	{
		CustomLink link = linkService.getByUuid(uuid);
		if( link != null )
		{
			linkService.moveLink(link.getUuid(), order);
			clearCache();
		}

		return "{status:'ok'}";
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		String sessionId = model.getSessionId();
		model.setRendered(true);
		if( !Check.isEmpty(sessionId) )
		{
			try
			{
				CustomLinkEditingSession session = linkService.loadSession(sessionId);
				loadInternal(info, session);
			}
			catch( WizardTimeoutException e )
			{
				model.setSessionId(null);
			}
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void saveToSession(SectionInfo info)
	{
		CustomLinksModel model = getModel(info);
		String sessionId = model.getSessionId();
		if( model.isRendered() && !Check.isEmpty(sessionId) )
		{
			try
			{
				CustomLinkEditingSession session = linkService.loadSession(sessionId);
				saveInternal(info, session);
			}
			catch( WizardTimeoutException e )
			{
				model.setSessionId(null);
			}
		}
	}

	private void loadInternal(SectionInfo info, CustomLinkEditingSession session)
	{
		final CustomLinkEditingBean link = session.getBean();

		LanguageBundleBean name = link.getName();
		if( name != null )
		{
			displayNameField.setLanguageBundle(info, name);
		}
		urlField.setValue(info, link.getUrl());
		newWindow.setChecked(info, link.getAttribute("newWindow", false));

		String expression = link.getTargetExpression();
		if( expression != null )
		{
			selector.setExpression(info, expression);
		}
		String fileName = link.getFileName();
		if( fileName != null )
		{
			getModel(info).setFileName(fileName);
		}
	}

	private CustomLinkEditingSession saveInternal(SectionInfo info, CustomLinkEditingSession session)
	{
		EntityPack<CustomLink> pack = session.getPack();
		CustomLinkEditingBean link = session.getBean();
		try
		{
			upload(getContext(info), true);
		}
		catch( IOException e )
		{
			// do nothing
		}
		link.setName(displayNameField.getLanguageBundle(info));
		link.setUrl(urlField.getValue(info));
		link.setAttribute("newWindow", String.valueOf(newWindow.isChecked(info)));
		link.setOrder(pack.getEntity().getOrder());
		// But...why?
		// link.setAttribute(FILE_NAME_KEY, session.getFileName());
		String expression = selector.getExpression(info);
		if( expression != null )
		{
			TargetList list = new TargetList();
			if( Check.isEmpty(expression) )
			{
				list.setEntries(null);
			}
			else
			{
				list.setEntries(new ArrayList<TargetListEntry>());
				list.getEntries().add(new TargetListEntry(true, false, "VIEW_CUSTOM_LINK", expression)); //$NON-NLS-1$
			}
			pack.setTargetList(list);
		}

		session.setValid(validate(info, session.getValidationErrors()));
		linkService.saveSession(session);
		return session;
	}

	private boolean validate(SectionInfo info, Map<String, Object> errors)
	{
		errors.clear();
		LanguageBundleBean bundle = displayNameField.getLanguageBundle(info);
		if( LangUtils.isEmpty(bundle) )
		{
			errors.put("displayNameField", CurrentLocale.get(ERROR_NAME_KEY));
		}

		if( Check.isEmpty(urlField.getValue(info)) || urlField.getValue(info).equalsIgnoreCase("http://") )
		{
			errors.put("urlField", CurrentLocale.get(ERROR_URL_KEY));
		}

		return errors.isEmpty();
	}

	public String getIconUrl(SectionInfo info, String uuid)
	{
		CustomLinkEditingSession session = linkService.loadSession(getModel(info).getSessionId());
		CustomLinkEditingBean bean = session.getBean();

		String fileName = bean.getFileName();
		if( Check.isEmpty(fileName) )
		{
			return null;
		}

		final String href = new BookmarkAndModify(info,
			eventFactory.getNamedModifier("getIcon", session.getStagingId(), fileName)).getHref();
		return urlService.removeInstitution(href);
	}

	private URL parseHtmlForFav(String url) throws Exception
	{
		HttpMethodBase method;
		HttpClient client;

		if( !url.toLowerCase().startsWith("http") )
		{
			method = new GetMethod(urlService.institutionalise(url));
		}
		else
		{
			method = new GetMethod(url);
		}

		method.setFollowRedirects(true);
		client = new HttpClient();
		client.getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
		client.getParams().setParameter(HttpClientParams.MAX_REDIRECTS, 10);
		client.executeMethod(method);

		CustomLinkContentHandler handler;
		try (ByteLimitedInputStream remoteStream = new ByteLimitedInputStream(method.getResponseBodyAsStream(),
			MAX_BYTES))
		{
			XMLReader r = new Parser();
			InputSource s = new InputSource(remoteStream);

			handler = new CustomLinkContentHandler();
			r.setContentHandler(handler);
			r.parse(s);
		}

		String baseUri = handler.getBaseUri();
		String iconUrl = handler.getIconUrl();

		URL fullUrl;
		URL base;
		if( !Check.isEmpty(baseUri) )
		{
			base = new URL(baseUri);
		}
		else
		{
			if( !url.toLowerCase().startsWith("http") )
			{
				base = urlService.getInstitutionUrl();
			}
			else
			{
				base = new URL(url);
			}
		}

		if( !Check.isEmpty(iconUrl) )
		{
			fullUrl = new URL(base, iconUrl);
		}
		else
		{
			fullUrl = new URL(base, "favicon.ico");
		}

		return fullUrl;
	}

	private List<CustomLinkListComponent> createLinkList()
	{
		List<CustomLinkListComponent> list = new ArrayList<CustomLinkListComponent>();

		for( CustomLink link : linkService.enumerateInOrder() )
		{
			String uuid = link.getUuid();
			HtmlLinkState delete = null;
			HtmlLinkState edit = new HtmlLinkState(EDIT_LABEL);

			String iconUrl = null;
			String fileName = link.getAttribute(FILE_NAME_KEY);
			if( !Check.isEmpty(fileName) )
			{
				try
				{
					fileName = new URI(null, null, fileName, null).toString();
				}
				catch( URISyntaxException e )
				{
					// nothing
				}

				iconUrl = urlService.institutionalise("entity/" + link.getId() + "/" + fileName);
			}

			if( linkService.canDelete(link) )
			{
				delete = new HtmlLinkState(DELETE_LABEL);
				delete.setClickHandler(new OverrideHandler(deleteUrlFunc, uuid).addValidator(DELETE_CONFIRM));
			}

			edit.setClickHandler(new OverrideHandler(editUrlFunc, uuid));
			list.add(new CustomLinkListComponent(link.toString(), uuid, edit, delete, iconUrl));
		}

		return list;
	}

	private void clearCache()
	{
		menuContributor.clearCachedData();
	}

	@Override
	public Class<CustomLinksModel> getModelClass()
	{
		return CustomLinksModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "cls";
	}

	public Button getAddButton()
	{
		return addButton;
	}

	public TextField getUrlField()
	{
		return urlField;
	}

	public MultiEditBox getDisplayNameField()
	{
		return displayNameField;
	}

	public Checkbox getNewWindow()
	{
		return newWindow;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public ExpressionSelectorDialog getSelector()
	{
		return selector;
	}

	public FileUpload getFile()
	{
		return file;
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}

	public Button getDeleteIconButton()
	{
		return deleteIconButton;
	}

	public ImageRenderer getImage()
	{
		return image;
	}

	public Button getDownloadButton()
	{
		return downloadButton;
	}

	public DivRenderer getLinkDiv()
	{
		return linkDiv;
	}
}
