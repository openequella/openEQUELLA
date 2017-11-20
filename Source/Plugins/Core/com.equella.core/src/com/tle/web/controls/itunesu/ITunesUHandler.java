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

package com.tle.web.controls.itunesu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.wizard.controls.universal.handlers.ITunesUSettings;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonSize;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonTrait;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.PreRenderOnly;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.HeadingRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

import edu.asu.itunesu.Course;
import edu.asu.itunesu.Division;
import edu.asu.itunesu.Group;
import edu.asu.itunesu.ITunesUConnection;
import edu.asu.itunesu.ITunesUException;
import edu.asu.itunesu.Section;
import edu.asu.itunesu.Site;
import edu.asu.itunesu.Track;

@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class ITunesUHandler extends BasicAbstractAttachmentHandler<ITunesUHandler.ITunesUHandlerModel>
{
	public static final String ITUNESU_TYPE = "itunesu";
	public static final String ITUNESU_URL = "trackUrl";
	public static final String ITUNESU_TRACK = "trackName";

	@PlugKey("itunesu.name")
	private static Label NAME_LABEL;
	@PlugKey("itunesu.description")
	private static Label DESCRIPTION_LABEL;
	@PlugKey("itunesu.add.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("error.configproblem.heading")
	private static Label CONFIG_ERROR_HEADING;
	@PlugKey("itunesu.edit.title")
	private static Label EDIT_TITLE_LABEL;
	@PlugKey("add.add")
	private static Label ADD_LABEL;
	@PlugKey("error.noinstitutionid")
	private static Label ERROR_NO_INST;
	@PlugKey("error.badinstitutionid")
	private static String BAD_ID_ERROR;

	@PlugKey("itunesu.details.viewlink")
	private static Label VIEW_LINK_LABEL;

	@Inject
	private AttachmentResourceService attachmentResourceService;

	@AjaxFactory
	private AjaxGenerator ajaxFactory;

	@Component
	private Tree treeView;

	private ITunesUSettings iTunesUSettings;

	private String institutionId;
	private ITunesTreeModel itunesModel;

	private JSCallable showTreeFunc;
	private JSCallable addTrackFunc;

	@Override
	public String getHandlerId()
	{
		return "iTunesUHandler";
	}

	private String getUrlForTrack(String trackId)
	{
		return "http://deimos.apple.com/WebObjects/Core.woa/Browsev2/" + institutionId + "." + trackId;
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		super.onRegister(tree, parentId, state);
		iTunesUSettings = new ITunesUSettings(state.getControlConfiguration());
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		showTreeFunc = ajaxFactory.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("loadTree"),
			ajaxFactory.getEffectFunction(EffectType.FADEIN), "itunesTree");
		addTrackFunc = events.getSubmitValuesFunction("addTrack");
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		institutionId = iTunesUSettings.getInstitutionId();
		itunesModel = new ITunesTreeModel(new ITunesUConnection(
			"https://deimos.apple.com/WebObjects/Core.woa/Browsev2/" + institutionId, "", "", new String[]{""}));

		treeView.setModel(itunesModel);
		treeView.setLazyLoad(true);
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
			return ITUNESU_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		// Common details
		ITunesUHandlerModel model = getModel(context);
		final Attachment attachment = getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			attachment);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Listen link
		String href = (String) attachment.getData("trackUrl");
		HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark(href));
		linkState.setTarget(HtmlLinkState.TARGET_BLANK);
		model.setViewlink(new LinkRenderer(linkState));
		return viewFactory.createResult("edit-itunesu.ftl", this);
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		if( !itunesModel.isInitialised() && itunesModel.checkBadId() )
		{
			renderOptions.setShowAddReplace(false);
			renderOptions.setShowSave(false);
			HeadingRenderer heading = new HeadingRenderer(3, CONFIG_ERROR_HEADING);
			LabelRenderer error;
			if( Check.isEmpty(iTunesUSettings.getInstitutionId()) )
			{
				error = new LabelRenderer(ERROR_NO_INST);
			}
			else
			{
				error = new LabelRenderer(new KeyLabel(BAD_ID_ERROR, iTunesUSettings.getInstitutionId()));
			}

			return new CombinedRenderer(heading, error);
		}

		if( !itunesModel.isInitialised() )
		{
			getModel(context).setLoading(true);
			treeView.addReadyStatements(context, showTreeFunc);
		}
		else
		{
			treeView.addReadyStatements(context, new PreRenderOnly(addTrackFunc));
		}
		renderOptions.setShowSave(false);
		return viewFactory.createResult("add-itunesu.ftl", this);
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		Attachment rv = getModel(info).getNewTrack();
		if( rv != null )
		{
			return Collections.singletonList(rv);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isMultipleAllowed(SectionInfo info)
	{
		return false;
	}

	@EventHandlerMethod
	public void addTrack(SectionInfo info, String trackName, String trackId)
	{
		final CustomAttachment a = new CustomAttachment();
		a.setType(ITUNESU_TYPE);
		a.setData(ITUNESU_URL, getUrlForTrack(trackId));
		a.setData(ITUNESU_TRACK, trackName);
		a.setDescription(trackName);
		getModel(info).setNewTrack(a);
		dialogState.save(info);
	}

	@EventHandlerMethod
	public void loadTree(SectionInfo info)
	{
		if( Check.isEmpty(institutionId) )
		{
			throw new RuntimeException(ERROR_NO_INST.getText());
		}
		itunesModel.ensureInit();
	}

	public Tree getTreeView()
	{
		return treeView;
	}

	@Override
	public Class<ITunesUHandlerModel> getModelClass()
	{
		return ITunesUHandlerModel.class;
	}

	@NonNullByDefault(false)
	public static class ITunesUHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		private boolean loading;
		private CustomAttachment newTrack;

		public boolean isLoading()
		{
			return loading;
		}

		public void setLoading(boolean loading)
		{
			this.loading = loading;
		}

		public CustomAttachment getNewTrack()
		{
			return newTrack;
		}

		public void setNewTrack(CustomAttachment newTrack)
		{
			this.newTrack = newTrack;
		}
	}

	public class ITunesTreeModel implements HtmlTreeModel
	{
		private final Map<String, List<HtmlTreeNode>> childNodes = new HashMap<String, List<HtmlTreeNode>>();
		private boolean initialised;
		private final ITunesUConnection connection;

		public ITunesTreeModel(ITunesUConnection connection)
		{
			this.connection = connection;
		}

		public boolean isInitialised()
		{
			return initialised;
		}

		public synchronized void ensureInit()
		{
			if( !initialised )
			{
				try
				{
					Site site = connection.getSite();
					addNode(new ITunesNode(this, site), "");
				}
				catch( ITunesUException e )
				{
					throw new RuntimeException(new KeyLabel(BAD_ID_ERROR, iTunesUSettings.getInstitutionId()).getText());
				}
				initialised = true;
			}
		}

		public boolean checkBadId()
		{
			try
			{
				connection.getSite();
				return false;
			}
			catch( ITunesUException e )
			{
				return true;
			}
		}

		public void addNode(ITunesNode node, String parent)
		{
			List<HtmlTreeNode> list = childNodes.get(parent);
			if( list == null )
			{
				list = new ArrayList<HtmlTreeNode>();
				childNodes.put(parent, list);
			}
			list.add(node);
		}

		@Override
		public List<HtmlTreeNode> getChildNodes(@Nullable SectionInfo info, @Nullable String id)
		{
			ensureInit();
			if( id == null )
			{
				id = "";
			}
			List<HtmlTreeNode> list = childNodes.get(id);
			if( list == null )
			{
				return Collections.emptyList();
			}
			return list;
		}
	}

	public class ITunesNode implements HtmlTreeNode
	{
		private String id;
		@Nullable
		private Label name;
		private SectionRenderable renderer;
		private boolean leaf;
		private boolean shouldHide;
		@Nullable
		private String textName;

		public ITunesNode(ITunesTreeModel tunesTreeModel, Object dodge)
		{
			List<?> children = null;
			if( dodge instanceof Site )
			{
				Site site = (Site) dodge;
				id = 'i' + site.getHandle();
				children = site.getSections();
				textName = site.getName();
			}
			else if( dodge instanceof Section )
			{
				Section section = (Section) dodge;
				id = 's' + section.getHandle();
				children = section.getSectionItems();
				textName = section.getName();
			}
			else if( dodge instanceof Division )
			{
				Division division = (Division) dodge;
				id = 'd' + division.getHandle();
				children = division.getSections();
				textName = division.getName();
			}
			else if( dodge instanceof Course )
			{
				Course course = (Course) dodge;
				id = 'c' + course.getHandle();
				children = course.getGroups();
				textName = course.getName();
			}
			else if( dodge instanceof Group )
			{
				Group group = (Group) dodge;
				id = 'g' + group.getHandle();
				children = group.getTracks();
				textName = group.getName();
			}
			else if( dodge instanceof Track )
			{
				Track track = (Track) dodge;
				id = 't' + track.getHandle();
				leaf = true;
				textName = track.getName();
				HtmlLinkState link = new HtmlLinkState(new SimpleBookmark(getUrlForTrack(track.getHandle())));
				link.setLabel(getLabel());
				PopupLinkRenderer viewLink = new PopupLinkRenderer(link);
				HtmlComponentState addButton = new HtmlComponentState(new OverrideHandler(addTrackFunc,
					track.getName(), track.getHandle()));
				addButton.setLabel(ADD_LABEL);
				renderer = new DivRenderer(new CombinedRenderer(viewLink, new SimpleSectionResult(" "),
					new ButtonRenderer(addButton).setTrait(ButtonTrait.SUCCESS).setIcon(Icon.ADD)
						.setSize(ButtonSize.SMALL)));
			}
			if( textName == null )
			{
				shouldHide = true;
			}
			boolean hasKids = false;
			if( children != null )
			{
				for( Object child : children )
				{
					ITunesNode childNode = new ITunesNode(tunesTreeModel, child);
					if( !childNode.isShouldHide() )
					{
						tunesTreeModel.addNode(childNode, id);
						hasKids = true;
					}
				}
			}
			shouldHide |= !leaf && !hasKids;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public Label getLabel()
		{
			if( name == null )
			{
				name = new TextLabel(textName);
			}
			return name;
		}

		@Override
		public SectionRenderable getRenderer()
		{
			return renderer;
		}

		@Override
		public boolean isLeaf()
		{
			return leaf;
		}

		public boolean isShouldHide()
		{
			return shouldHide;
		}
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return "equella/attachment-itunesu";
	}
}
