package com.tle.web.controls.kaltura;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMediaListResponse;
import com.kaltura.client.types.KalturaUiConf;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Utils;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.admin.control.KalturaSettings;
import com.tle.common.kaltura.admin.control.KalturaSettings.KalturaOption;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.i18n.BundleCache;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryUICore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.Pager;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.sections.standard.renderers.HeadingRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.sections.swfobject.SwfObject;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind
@NonNullByDefault
@SuppressWarnings("nls")
public class KalturaHandler extends BasicAbstractAttachmentHandler<KalturaHandler.KalturaHandlerModel>
{
	private static final int PER_PAGE = 10;

	private static final CssInclude CSS = CssInclude
		.include(ResourcesService.getResourceHelper(KalturaHandler.class).url("css/kaltura.css")).hasRtl().make();

	@PlugURL("images/kalturalogotrans.png")
	private static String KALTURA_LOGO_URL;

	@PlugURL("js/kaltura.js")
	private static String KALTURA;
	@PlugURL("js/kalturaopts.js")
	private static String KALTURA_OPTS;

	@PlugKey("choice.")
	private static String KEY_PREFIX_CHOICES;

	@PlugKey("name")
	private static Label NAME_LABEL;
	@PlugKey("description")
	private static Label DESCRIPTION_LABEL;

	@PlugKey("add.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("add.views.singular")
	private static String SINGULAR_VIEWS_LABEL;
	@PlugKey("add.views.plural")
	private static String PLURAL_VIEWS_LABEL;
	@PlugKey("add.query.empty")
	private static Label EMPTY_QUERY_LABEL;
	@PlugKey("edit.title")
	private static Label EDIT_TITLE_LABEL;

	@PlugKey("uploaded.tags")
	private static String UPLOAD_TAGS_LABEL;
	@PlugKey("details.views")
	private static Label VIEWS_LABEL;
	@PlugKey("details.viewlink")
	private static Label VIEW_LINK_LABEL;
	@PlugKey("details.downloadlink")
	private static Label DOWNLOAD_LINK_LABEL;
	@PlugKey("details.duration.seconds.singular")
	private static Label SECONDS_SINGULAR;
	@PlugKey("details.duration.seconds.plural")
	private static String SECONDS_PLURAL;
	@PlugKey("details.duration")
	private static Label DURATION;

	@PlugKey("info.unavailable")
	private static String KEY_UNAVAILABLE;
	@PlugKey("info.unavailable.desc")
	private static String KEY_UNAVAILABLE_DESC;
	@PlugKey("info.")
	private static String KEY_INFO_PREFIX;
	@PlugKey("info.desc.")
	private static String KEY_INFO_DESC_PREFIX;

	@PlugKey("edit.players.default")
	private static Label SERVER_DEFAULT;

	private JSCallable setupKcwControl;
	private JSCallable finishedCallback;

	@Inject
	private KalturaService kalturaService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private TextField query;
	@Component
	@PlugKey("add.search.button")
	private Button search;

	@Component(name = "confid")
	private SingleSelectionList<KalturaUiConf> players;

	@PlugKey("action.next")
	@Component
	private Button nextChoiceButton;

	@Component
	private MultiSelectionList<Void> results;
	@Component
	private Pager pager;
	@Component
	private MultiSelectionList<KalturaUpload> selections;
	@PlugKey("uploaded.link.selectall")
	@Component
	private Link selectAll;
	@PlugKey("uploaded.link.selectnone")
	@Component
	private Link selectNone;

	@Component
	private SingleSelectionList<KalturaOption> choice;

	@Component
	private Div divKcw;
	@Component
	private Div divKdp;

	private KalturaSettings kalturaSettings;

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		super.onRegister(tree, parentId, state);
		kalturaSettings = new KalturaSettings(state.getControlConfiguration());
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		choice.setListModel(new EnumListModel<KalturaOption>(KEY_PREFIX_CHOICES, ".desc", true, KalturaOption.EXISTING,
			KalturaOption.UPLOAD));

		search.setClickHandler(new OverrideHandler(events.getNamedHandler("searchClicked")).addValidator(
			new SimpleValidator(new NotEqualsExpression(query.createGetExpression(), new StringExpression("")))
				.setFailureStatements(Js.alert_s(EMPTY_QUERY_LABEL))));

		nextChoiceButton.setClickHandler(new ReloadHandler());
		setupKcwControl = new ExternallyDefinedFunction("setupKCW", new IncludeFile(KALTURA), SwfObject.PRERENDER);

		finishedCallback = events.getSubmitValuesFunction("finished");

		JSCallable setAllFunction = selections.createSetAllFunction();
		selectAll.setClickHandler(new OverrideHandler(setAllFunction, true));
		selectNone.setClickHandler(new OverrideHandler(setAllFunction, false));

		results.setListModel(new DynamicHtmlListModel<Void>()
		{
			@Override
			protected Iterable<Option<Void>> populateOptions(SectionInfo info)
			{
				KalturaServer ks = getKalturaServer();
				KalturaHandlerModel model = getModel(info);

				if( model.isButtonUpdate() )
				{
					return Collections.emptyList();
				}

				String value = query.getValue(info);
				String q = !Check.isEmpty(value) ? value : "";
				if( Check.isEmpty(q) )
				{
					return null;
				}

				KalturaMediaListResponse mediaList = kalturaService.searchMedia(
					getKalturaClient(ks, KalturaSessionType.ADMIN), Lists.newArrayList(q), pager.getCurrentPage(info),
					PER_PAGE);

				if( mediaList == null || mediaList.totalCount == 0 )
				{
					return null;
				}

				pager.setup(info, (mediaList.totalCount - 1) / PER_PAGE + 1, PER_PAGE);

				final List<Option<Void>> rv = new ArrayList<Option<Void>>();
				String uiConfId = getKdpUiConfId(info, ks, false);

				for( KalturaMediaEntry entry : mediaList.objects )
				{
					KalturaResultOption result = new KalturaResultOption(entry.id);

					final LinkRenderer titleLink = new PopupLinkRenderer(
						new HtmlLinkState(new SimpleBookmark(createFlashEmbedUrl(ks, entry.id, uiConfId))));

					titleLink.setLabel(new TextLabel(entry.name));
					result.setLink(titleLink);
					result.setDescription(new TextLabel(entry.description));
					result.setDate(dateRendererFactory.createDateRenderer(new Date(entry.createdAt * 1000L)));
					result.setThumbnail(new ImageRenderer(entry.thumbnailUrl, new TextLabel(entry.name)));
					int views = entry.views;
					result.setViews(
						views == 1 ? new KeyLabel(SINGULAR_VIEWS_LABEL) : new KeyLabel(PLURAL_VIEWS_LABEL, views));

					rv.add(result);
				}
				return rv;
			}

			@Override
			protected Iterable<Void> populateModel(SectionInfo info)
			{
				return null;
			}
		});

		selections.setListModel(new DynamicHtmlListModel<KalturaUpload>()
		{
			@Override
			protected Iterable<KalturaUpload> populateModel(SectionInfo info)
			{
				KalturaHandlerModel model = getModel(info);
				KalturaServer ks = getKalturaServer();

				if( model.isButtonUpdate() || Check.isEmpty(model.getUploads()) )
				{
					return Collections.emptyList();
				}

				KalturaMediaListResponse mediaList = kalturaService.getMediaEntries(
					getKalturaClient(ks, KalturaSessionType.ADMIN),
					Lists.transform(model.getUploads(), new Function<KalturaUploadInfo, String>()
				{
					@Override
					public String apply(KalturaUploadInfo input)
					{
						return input.getEntryId();
					}

				}));

				final List<KalturaUpload> rv = new ArrayList<KalturaUpload>();
				String uiConfId = getKdpUiConfId(info, ks, false);

				for( KalturaMediaEntry entry : mediaList.objects )
				{
					KalturaUpload uo = new KalturaUpload(entry.id);

					final LinkRenderer titleLink = new PopupLinkRenderer(
						new HtmlLinkState(new SimpleBookmark(createFlashEmbedUrl(ks, entry.id, uiConfId))));

					titleLink.setLabel(new TextLabel(entry.name));
					uo.setTitle(titleLink);
					uo.setDescription(new TextLabel(entry.description));
					String tags = entry.tags;
					if( !Check.isEmpty(tags) )
					{
						uo.setTags(new KeyLabel(UPLOAD_TAGS_LABEL, tags));
					}

					rv.add(uo);
				}

				return rv;
			}

			@Override
			protected Option<KalturaUpload> convertToOption(SectionInfo info, KalturaUpload obj)
			{
				return new KeyOption<KalturaHandler.KalturaUpload>(null, obj.getVideoId(), obj);
			}
		});

		players.setListModel(new DynamicHtmlListModel<KalturaUiConf>()
		{
			@Override
			protected Iterable<KalturaUiConf> populateModel(SectionInfo info)
			{
				return kalturaService.getPlayers(getKalturaServer());
			}

			@Override
			protected Option<KalturaUiConf> getTopOption()
			{
				return new LabelOption<KalturaUiConf>(SERVER_DEFAULT, "", null);
			}

			@Override
			protected Option<KalturaUiConf> convertToOption(SectionInfo info, KalturaUiConf conf)
			{
				return new SimpleOption<KalturaUiConf>(conf.name, Integer.toString(conf.id), conf);
			}
		});

		players.addChangeEventHandler(ajax.getAjaxUpdateDomFunction(tree, null, null, "mediapreview"));

		pager.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		StatementHandler updateHandler = new StatementHandler(
			dialogState.getDialog().getFooterUpdate(tree, events.getEventHandler("updateButtons")));

		results.setEventHandler(JSHandler.EVENT_CHANGE, updateHandler);
		selections.setEventHandler(JSHandler.EVENT_CHANGE, updateHandler);
	}

	private String createFlashEmbedUrl(KalturaServer ks, String entryId, String uiConfId)
	{
		return MessageFormat.format("{0}/kwidget/wid/_{1}/uiconf_id/{2}/entry_id/{3}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), uiConfId, entryId);
	}

	private String getKdpUiConfId(SectionInfo info, KalturaServer ks, boolean useCustom)
	{
		// Player selection
		if( useCustom )
		{
			String uiConfId = players.getSelectedValueAsString(info);
			if( !Check.isEmpty(uiConfId) && kalturaService.hasConf(ks, uiConfId) )
			{
				return uiConfId;
			}
		}

		// Server default
		String uiConfId = Integer.toString(ks.getKdpUiConfId());
		if( !Check.isEmpty(uiConfId) && kalturaService.hasConf(ks, uiConfId) )
		{
			return uiConfId;
		}

		// EQUELLA default
		return Integer.toString(kalturaService.getDefaultKdpUiConf(ks).id);
	}

	public KalturaClient getKalturaClient(KalturaServer ks, KalturaSessionType type)
	{
		try
		{
			return kalturaService.getKalturaClient(ks, type);
		}
		catch( KalturaApiException e )
		{
			SectionUtils.throwRuntime(e);
		}

		return null;
	}

	@Nullable
	private KalturaServer getKalturaServer()
	{
		return kalturaService.getByUuid(kalturaSettings.getServerUuid());
	}

	@Override
	protected boolean isOnePageAdd()
	{
		return false;
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		KalturaHandlerModel model = getModel(context);
		SectionRenderable renderable;

		KalturaServer ks = getKalturaServer();
		if( ks == null || !ks.isEnabled() || !kalturaService.isUp(ks) )
		{
			String key = ks == null ? "missing" : !ks.isEnabled() ? "disabled" : "offline";
			HeadingRenderer heading = new HeadingRenderer(3,
				new KeyLabel(KEY_UNAVAILABLE, new KeyLabel(KEY_INFO_PREFIX + key)));
			LabelRenderer error = new LabelRenderer(
				new KeyLabel(KEY_UNAVAILABLE_DESC, new KeyLabel(KEY_INFO_DESC_PREFIX + key)));

			ImageRenderer watermark = new ImageRenderer(new TagState("kaltura-logo"), KALTURA_LOGO_URL,
				new TextLabel("kalturalogo"));

			return new CombinedRenderer(heading, error, watermark, CSS);
		}

		model.setKalturaServer(new BundleLabel(ks.getName().getId(), bundleCache));

		if( !isMultipleAllowed(context) )
		{
			results.getState(context).setDisallowMultiple(true);
			selections.getState(context).setDisallowMultiple(true);
		}

		// Check if there is a restriction on the choice
		String restriction = kalturaSettings.getRestriction();
		KalturaOption choiceOption = null;

		if( restriction != null )
		{
			choiceOption = KalturaOption.valueOf(restriction);
		}
		else
		{
			choiceOption = choice.getSelectedValue(context);
		}

		if( choiceOption == null )
		{
			// Render Choice
			renderable = renderChoice(context, renderOptions);
		}
		else
		{
			// Render KCW or Search
			if( choiceOption == KalturaOption.EXISTING )
			{
				renderable = renderSearch(context, renderOptions);
			}
			else
			{
				// Render List of uploads or KCW
				if( model.isFinishedUploading() )
				{
					renderable = renderUploads(context, renderOptions);
				}
				else
				{
					// Generate user client ks and flashvars etc. pass to setup
					setupKalturaKcw(context);
					renderable = renderContribution();
				}
			}
		}

		return new CombinedRenderer(renderable, CSS);
	}

	private void setupKalturaKcw(RenderContext context)
	{
		KalturaServer ks = getKalturaServer();
		ObjectExpression kcwVars = new ObjectExpression();
		kcwVars.put("ep", ks.getEndPoint());
		kcwVars.put("pid", ks.getPartnerId());
		kcwVars.put("flashVersion", "9.0.0");
		kcwVars.put("width", "775");
		kcwVars.put("height", "380");

		KalturaClient kclientAdmin = getKalturaClient(ks, KalturaSessionType.ADMIN);
		kcwVars.put("uiConfId", Integer.toString(kalturaService.getDefaultKcwUiConf(kclientAdmin).id));
		kcwVars.put("ks", getKalturaClient(ks, KalturaSessionType.USER).getSessionId());
		kcwVars.put("onClose", finishedCallback);

		divKcw.addReadyStatements(context, new FunctionCallStatement(
			new FunctionCallExpression(setupKcwControl, divKcw.getElementId(context), kcwVars)));
	}

	private void setupKalturaKdp(SectionInfo context, KalturaServer ks, String flashUrl)
	{
		ObjectExpression kdpVars = new ObjectExpression();

		kdpVars.put("flashVersion", "9.0.0");
		kdpVars.put("width", "300");
		kdpVars.put("height", "200");
		kdpVars.put("embedUrl", flashUrl);

		divKdp.addReadyStatements(context,
			new FunctionCallStatement(new FunctionCallExpression(new ExternallyDefinedFunction("setupKDP",
				SwfObject.PRERENDER, new IncludeFile(KALTURA), new IncludeFile(createHtml5embed(ks))),
			divKdp.getElementId(context), kdpVars)));
	}

	private SectionRenderable renderChoice(RenderContext context, DialogRenderOptions renderOptions)
	{
		renderOptions.addAction(nextChoiceButton);
		choice.addReadyStatements(context,
			new ExternallyDefinedFunction("setupOpts", new IncludeFile(KALTURA_OPTS, JQueryUICore.PRERENDER)),
			nextChoiceButton.getState(context));
		getModel(context).setKalturaLogo(
			new ImageRenderer(new TagState("kaltura-logo"), KALTURA_LOGO_URL, new TextLabel("kalturalogo")));
		return viewFactory.createResult("option-kaltura.ftl", this);
	}

	private SectionRenderable renderSearch(RenderContext context, DialogRenderOptions renderOptions)
	{
		renderOptions.setShowSave(!Check.isEmpty(results.getSelectedValuesAsStrings(context)));
		renderOptions.setShowAddReplace(true);

		return viewFactory.createResult("add-kaltura.ftl", this);
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		KalturaServer ks = getKalturaServer();

		KalturaHandlerModel model = getModel(context);
		final Attachment a = getDetailsAttachment(context);
		String embedUrl = createFlashEmbedUrl(ks, (String) a.getData(KalturaUtils.PROPERTY_ENTRY_ID),
			getKdpUiConfId(context, ks, true));
		model.addSpecificDetail("dataurl", new Pair<Label, Object>(null, embedUrl));
		model.setShowPlayers(players.getListModel().getOptions(context).size() > 1);

		// Setup preview embed
		setupKalturaKdp(context, ks, embedUrl);

		// Get common details from viewable resource
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			a);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Get dynamic details
		String entryId = (String) a.getData(KalturaUtils.PROPERTY_ENTRY_ID);
		if( !Check.isEmpty(entryId) )
		{
			// Get kaltura media entry
			KalturaMediaEntry entry = kalturaService.getMediaEntry(getKalturaClient(ks, KalturaSessionType.ADMIN),
				entryId);

			// Duration has to be dynamic as it is 0 when converting
			int duration = entry.duration;
			if( duration != 0 )
			{
				// Cannot cast from Integer to long
				String fd = Utils.formatDuration(duration);
				addAttachmentDetail(context, DURATION, fd.contains(":") ? new TextLabel(fd)
					: (fd.equals("1") ? SECONDS_SINGULAR : new KeyLabel(SECONDS_PLURAL, fd)));
			}

			addAttachmentDetail(context, VIEWS_LABEL, new NumberLabel(entry.views));

			HtmlLinkState linkState;
			String downloadUrl = entry.downloadUrl;
			if( !Check.isEmpty(downloadUrl) )
			{
				linkState = new HtmlLinkState(DOWNLOAD_LINK_LABEL, new SimpleBookmark(downloadUrl));
			}
			else
			{
				linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark(""));
			}
			linkState.setTarget(HtmlLinkState.TARGET_BLANK);
			model.setViewlink(new LinkRenderer(linkState));
		}

		return new CombinedRenderer(viewFactory.createResult("edit-kaltura.ftl", this), CSS);
	}

	private SectionRenderable renderContribution()
	{
		return viewFactory.createResult("contribute-kaltura.ftl", this);
	}

	private SectionRenderable renderUploads(RenderContext context, DialogRenderOptions renderOptions)
	{
		renderOptions.setShowSave(selections.getSelectedValuesAsStrings(context).size() >= 1);
		renderOptions.setShowAddReplace(true);

		return viewFactory.createResult("uploads-kaltura.ftl", this);
	}

	private String createHtml5embed(KalturaServer ks)
	{
		return MessageFormat.format("{0}/p/{1}/embedIframeJs/uiconf_id/{2}/partner_id/{1}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), Integer.toString(ks.getKdpUiConfId()));
	}

	@EventHandlerMethod
	public void finished(SectionInfo info, List<KalturaUploadInfo> entries)
	{
		KalturaHandlerModel model = getModel(info);
		model.setFinishedUploading(true);
		model.addUploads(entries);

		if( entries.size() == 1 )
		{
			selections.setSelectedStringValue(info, entries.get(0).getEntryId());
			dialogState.save(info);
		}
	}

	@EventHandlerMethod
	public void searchClicked(SectionInfo info)
	{
		getModel(info).setSearchPerformed(!Check.isEmpty(query.getValue(info)));
	}

	// From the kcw flash widget
	public static class KalturaUploadInfo
	{
		private String mediaType;
		private String entryId;

		public KalturaUploadInfo()
		{
			// Nothing to see here
		}

		public KalturaUploadInfo(String mediaType, String entryId)
		{
			this.mediaType = mediaType;
			this.entryId = entryId;
		}

		public String getMediaType()
		{
			return mediaType;
		}

		public void setMediaType(String mediaType)
		{
			this.mediaType = mediaType;
		}

		public String getEntryId()
		{
			return entryId;
		}

		public void setEntryId(String entryId)
		{
			this.entryId = entryId;
		}
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		List<Attachment> attachments = Lists.newArrayList();
		List<String> entries = Collections.emptyList();

		String restriction = kalturaSettings.getRestriction();
		KalturaOption choiceOption = null;

		if( restriction != null )
		{
			choiceOption = KalturaOption.valueOf(restriction);
		}
		else
		{
			choiceOption = choice.getSelectedValue(info);
		}

		switch( choiceOption )
		{
			case EXISTING:
				entries = Lists.newArrayList(results.getSelectedValuesAsStrings(info));
				break;
			case UPLOAD:
				entries = Lists.newArrayList(selections.getSelectedValuesAsStrings(info));
				break;
		}
		for( String entryId : entries )
		{
			attachments.add(createAttachment(entryId));
		}
		return attachments;
	}

	private Attachment createAttachment(String entryId)
	{
		KalturaMediaEntry entry = kalturaService
			.getMediaEntry(getKalturaClient(getKalturaServer(), KalturaSessionType.ADMIN), entryId);

		CustomAttachment attachment = new CustomAttachment();

		attachment.setType(KalturaUtils.ATTACHMENT_TYPE);
		attachment.setDescription(entry.name); // Title

		attachment.setData(KalturaUtils.PROPERTY_KALTURA_SERVER, kalturaSettings.getServerUuid());
		attachment.setData(KalturaUtils.PROPERTY_DESCRIPTION, entry.description);
		attachment.setData(KalturaUtils.PROPERTY_DATE, entry.createdAt * 1000L);
		String thumbnailUrl = entry.thumbnailUrl;
		attachment.setData(KalturaUtils.PROPERTY_THUMB_URL, thumbnailUrl);
		attachment.setThumbnail(thumbnailUrl);
		attachment.setData(KalturaUtils.PROPERTY_ENTRY_ID, entry.id);
		attachment.setData(KalturaUtils.PROPERTY_TITLE, entry.name);
		attachment.setData(KalturaUtils.PROPERTY_DURATION, (long) entry.duration);
		attachment.setData(KalturaUtils.PROPERTY_TAGS, entry.tags);

		return attachment;
	}

	@Override
	protected void setupDetailEditing(SectionInfo info)
	{
		super.setupDetailEditing(info);
		Attachment attachment = getDetailsAttachment(info);
		String playerId = (String) attachment.getData(KalturaUtils.PROPERTY_CUSTOM_PLAYER);
		players.setSelectedStringValue(info, playerId != null ? playerId : "");
	}

	@Override
	protected void saveDetailsToAttachment(SectionInfo info, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, attachment);
		KalturaUiConf conf = players.getSelectedValue(info);
		if( conf != null )
		{
			attachment.setData(KalturaUtils.PROPERTY_CUSTOM_PLAYER, Integer.toString(conf.id));
		}
		else
		{
			attachment.getDataAttributes().remove(KalturaUtils.PROPERTY_CUSTOM_PLAYER);
		}
	}

	@Override
	public void cancelled(SectionInfo info)
	{
		super.cancelled(info);
		KalturaHandlerModel model = getModel(info);
		choice.setSelectedStringValue(info, null);
		model.setFinishedUploading(false);
		model.setUploads(new ArrayList<KalturaUploadInfo>());
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME_LABEL, DESCRIPTION_LABEL);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment ca = (CustomAttachment) attachment;
			return KalturaUtils.ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public String getHandlerId()
	{
		return "kalturaHandler";
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	public Class<KalturaHandlerModel> getModelClass()
	{
		return KalturaHandlerModel.class;
	}

	public static class KalturaHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		@Bookmarked
		private List<KalturaUploadInfo> uploads = Lists.newArrayList();
		@Bookmarked
		private boolean finishedUploading;
		private boolean showPlayers;
		private boolean searchPerformed;
		private SectionRenderable kalturaLogo;
		private Label kalturaServer;

		public List<KalturaUploadInfo> getUploads()
		{
			return uploads;
		}

		public void addUploads(List<KalturaUploadInfo> uploads)
		{
			for( KalturaUploadInfo ul : uploads )
			{
				this.uploads.add(ul);
			}
		}

		public boolean isFinishedUploading()
		{
			return finishedUploading;
		}

		public void setShowPlayers(boolean show)
		{
			this.showPlayers = show;
		}

		public boolean isShowPlayers()
		{
			return showPlayers;
		}

		public boolean isSearchPerformed()
		{
			return searchPerformed;
		}

		public void setSearchPerformed(boolean searchPerformed)
		{
			this.searchPerformed = searchPerformed;
		}

		public void setFinishedUploading(boolean finishedUploading)
		{
			this.finishedUploading = finishedUploading;
		}

		public void setUploads(List<KalturaUploadInfo> uploads)
		{
			this.uploads = uploads;
		}

		public SectionRenderable getKalturaLogo()
		{
			return kalturaLogo;
		}

		public void setKalturaLogo(SectionRenderable kalturaLogo)
		{
			this.kalturaLogo = kalturaLogo;
		}

		public Label getKalturaServer()
		{
			return kalturaServer;
		}

		public void setKalturaServer(Label kalturaServer)
		{
			this.kalturaServer = kalturaServer;
		}
	}

	public static class KalturaResultOption extends VoidKeyOption
	{
		private SectionRenderable thumbnail;
		private SectionRenderable date;
		private SectionRenderable link;
		private Label description;
		private Label author;
		private Label views;

		public KalturaResultOption(String videoId)
		{
			super(null, videoId);
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public void setAuthor(Label author)
		{
			this.author = author;
		}

		public void setViews(Label views)
		{
			this.views = views;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public void setDate(SectionRenderable date)
		{
			this.date = date;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public Label getAuthor()
		{
			return author;
		}

		public Label getViews()
		{
			return views;
		}

		public Label getDescription()
		{
			return description;
		}

		public SectionRenderable getDate()
		{
			return date;
		}

		public SectionRenderable getLink()
		{
			return link;
		}

		public void setLink(SectionRenderable link)
		{
			this.link = link;
		}
	}

	public static class KalturaUpload
	{
		private SectionRenderable title;
		private Label description;
		private Label tags;
		private final String videoId;

		public KalturaUpload(String videoId)
		{
			this.videoId = videoId;
		}

		public Label getDescription()
		{
			return description;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public SectionRenderable getTitle()
		{
			return title;
		}

		public void setTitle(SectionRenderable title)
		{
			this.title = title;
		}

		public Label getTags()
		{
			return tags;
		}

		public void setTags(Label tags)
		{
			this.tags = tags;
		}

		public String getVideoId()
		{
			return videoId;
		}
	}

	public SingleSelectionList<KalturaOption> getChoice()
	{
		return choice;
	}

	public TextField getQuery()
	{
		return query;
	}

	public Button getSearch()
	{
		return search;
	}

	public MultiSelectionList<Void> getResults()
	{
		return results;
	}

	public Pager getPager()
	{
		return pager;
	}

	public Div getDivKcw()
	{
		return divKcw;
	}

	public MultiSelectionList<KalturaUpload> getSelections()
	{
		return selections;
	}

	public Link getSelectAll()
	{
		return selectAll;
	}

	public Link getSelectNone()
	{
		return selectNone;
	}

	public Div getDivKdp()
	{
		return divKdp;
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	public SingleSelectionList<KalturaUiConf> getPlayers()
	{
		return players;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return KalturaUtils.MIME_TYPE;
	}
}
