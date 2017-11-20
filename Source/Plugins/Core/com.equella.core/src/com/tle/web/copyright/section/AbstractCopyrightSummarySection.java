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

package com.tle.web.copyright.section;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.activation.ActivationResultsExtension;
import com.tle.web.copyright.section.ViewByRequestSection.ViewRequestUrl;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.integration.extension.StructuredIntegrationSessionExtension;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.selection.section.CourseListSection;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewAttachmentUrl;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

import net.sf.json.JSONObject;

@NonNullByDefault
public abstract class AbstractCopyrightSummarySection<H extends Holding, P extends Portion, S extends Section>
	extends
		AbstractPrototypeSection<AbstractCopyrightSummarySection.Model>
	implements
		HtmlRenderer,
		AttachmentSelectorEventListener
{
	static
	{
		PluginResourceHandler.init(AbstractCopyrightSummarySection.class);
	}

	@PlugKey("summary.viewportion")
	private static Label LABEL_VIEWPORTION;
	@PlugKey("status.active")
	private static Label LABEL_STATUS_ACTIVE;
	@PlugKey("status.inactive")
	private static Label LABEL_STATUS_INACTIVE;
	@PlugKey("status.pending")
	private static Label LABEL_STATUS_PENDING;
	@PlugKey("summary.activate")
	private static Label LABEL_ACTIVATE;
	@PlugKey("summary.activateandadd")
	private static Label LABEL_ACTIVATE_ANDADD;
	@PlugKey("summary.unnamedportion")
	private static Label LABEL_UNNAMEDPORTION;
	@PlugKey("summary.restricted")
	private static Label LABEL_RESTRICTED_ATTACHMENT;

	@Inject
	private ItemService itemService;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemService viewItemService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private ActivationService activationService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private ActivationResultsExtension resultsExtension;

	private CopyrightService<H, P, S> copyrightService;
	private CopyrightWebService<H> copyrightWebService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private Button activateSelected;
	@Component
	private MappedBooleans selections;

	// Cannot be tree looked-up. CALSummarySection and CALActivate section are
	// inserted into the tree
	// at the same place as CLA versions, therefore we don't know which one
	// we'll get.
	// @TreeLookup
	private AbstractActivateSection activateSection;

	public static Label getActivateLabel()
	{
		return LABEL_ACTIVATE;
	}

	public static Label getActivateAndAddLabel()
	{
		return LABEL_ACTIVATE_ANDADD;
	}

	protected abstract String getChapterName(HoldingDisplay holdingDisplay, Portion portion);

	protected abstract String getPortionId(HoldingDisplay holdingDisplay, Portion portion);

	protected abstract HoldingDisplay createHoldingDisplay(Holding holding);

	protected abstract Class<? extends AbstractActivateSection> getActivateSectionClass();

	@PostConstruct
	void setupService()
	{
		copyrightService = getCopyrightServiceImpl();
		copyrightWebService = getCopyrightWebServiceImpl();
	}

	protected abstract CopyrightWebService<H> getCopyrightWebServiceImpl();

	protected abstract CopyrightService<H, P, S> getCopyrightServiceImpl();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		activateSelected.setClickHandler(events.getNamedHandler("activateAll")); //$NON-NLS-1$
		tree.addToListAttribute(AttachmentViewFilter.class, this);
		tree.addListener(null, AttachmentSelectorEventListener.class, this);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		activateSection = tree.lookupSection(getActivateSectionClass(), this);
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final Model model = getModel(context);
		if( isVisible(context) )
		{
			final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
			final long thisId = itemInfo.getItem().getId();
			final Holding holding = model.getItemHolding();
			final Item holdingItem = holding.getItem();

			HoldingDisplay holdingDisplay = createHoldingDisplay(holding);
			ItemNameLabel holdingName = new ItemNameLabel(holdingItem, bundleCache);
			List<PortionDisplay> portions = new ArrayList<PortionDisplay>();
			boolean holdingIsThis = holdingItem.getId() == thisId;
			holdingDisplay.setHoldingDisplay(holdingIsThis);
			Map<String, PortionDisplay> portionMap = new HashMap<String, PortionDisplay>();
			if( !holdingIsThis )
			{
				HtmlLinkState holdLink = getViewItemCommand(context, holdingItem, null);
				holdLink.setLabel(holdingName);
				holdingDisplay.setHoldingLink(new LinkRenderer(holdLink));
			}

			List<LinkRenderer> otherPortions = new ArrayList<LinkRenderer>();
			Set<Item> activatableItems = new HashSet<Item>();
			for( Portion portion : holding.getPortions() )
			{
				activatableItems.add(portion.getItem());
			}
			activatableItems = activationService.filterActivatableItems(activatableItems);

			for( Portion portion : holding.getPortions() )
			{
				Item item = portion.getItem();
				boolean thisItem = item.getId() == thisId;
				Label displayName = new BundleLabel(item.getName(), LABEL_UNNAMEDPORTION, bundleCache);
				if( holdingIsThis || thisItem )
				{
					String portionId = getPortionId(holdingDisplay, portion);

					PortionDisplay portionDisplay = portionMap.get(portionId);
					if( portionDisplay == null )
					{
						portionDisplay = new PortionDisplay();
						portionMap.put(portionId, portionDisplay);
						portions.add(portionDisplay);
					}
					portionDisplay.setChapter(getChapterName(holdingDisplay, portion));
					portionDisplay.setThisItem(thisItem);
					portionDisplay.setTitle(displayName);
					processSections(context, holdingDisplay, portion, portionDisplay, holdingIsThis,
						activatableItems.contains(item));
				}
				else
				{
					HtmlLinkState otherLink = getViewItemCommand(context, item, null);
					otherLink.setLabel(new ItemNameLabel(item, bundleCache));
					otherPortions.add(new LinkRenderer(otherLink));
				}
			}

			activateSelected.setDisplayed(context, holdingDisplay.isHasCheckboxes());

			sortPortions(holdingDisplay, portions, otherPortions);
			holdingDisplay.setOtherPortions(otherPortions);
			holdingDisplay.setPortions(portions);
			processAvailablePages(holding, holdingDisplay);
			model.setHolding(holdingDisplay);
			if( !holdingDisplay.getOtherPortions().isEmpty()
				&& !aclService.filterNonGrantedPrivileges(ActivationConstants.VIEW_LINKED_PORTIONS).isEmpty() )
			{
				model.setShowPortionLinks(true);
			}

			return viewFactory.createResult("copyright.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	private void processSections(RenderEventContext context, HoldingDisplay holdingDisplay, Portion portion,
		PortionDisplay portionDisplay, boolean holdingIsThis, boolean canActivate)
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		List<SectionDisplay> sections = portionDisplay.getSections();
		Item portionItem = portion.getItem();
		// CalInfo calInfo = getCalInfo(context);
		Map<String, Attachment> attachMap = copyrightWebService.getAttachmentMap(context, portionItem);
		NewDefaultViewableItem vitem = viewableItemFactory.createNewViewableItem(portionItem.getItemId());
		boolean integrating = integrationService.isInIntegrationSession(context);

		for( Section section : portion.getSections() )
		{
			String attachUuid = section.getAttachment();
			AttachmentId attachId = new AttachmentId(portionItem.getId(), attachUuid);
			String jsonAttachId = attachId.toJSONString();
			SectionDisplay sectionDisplay = new SectionDisplay();
			Attachment attachment = attachMap.get(attachUuid);
			if( attachment != null )
			{
				ViewableResource viewableResource = attachmentResourceService.getViewableResource(context, vitem,
					attachment);
				boolean restricted = viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN);
				LinkTagRenderer viewableLink;
				if( restricted )
				{
					viewableLink = new LinkRenderer(new HtmlLinkState(LABEL_RESTRICTED_ATTACHMENT));
					viewableLink.setDisabled(true);
					viewableLink.addClass("restricted-message");
				}
				else
				{
					viewableLink = viewItemService.getViewableLink(context, viewableResource, null);
					TextLabel textLabel = getAttachmentDisplayName(attachment);
					// JS EQ-2396 begin
					// attachment.setDescription(textLabel.getText());
					// JS EQ-2396 end
					sectionDisplay.setIcon(viewableResource.createStandardThumbnailRenderer(textLabel));
					viewableLink.setLabel(textLabel);
				}
				sectionDisplay.setViewLink(viewableLink);

				if( holdingIsThis
					&& !aclService.filterNonGrantedPrivileges(ActivationConstants.VIEW_LINKED_PORTIONS).isEmpty() )
				{
					HtmlLinkState portionLink = getViewItemCommand(context, portion.getItem(), null);
					portionLink.setLabel(LABEL_VIEWPORTION);
					sectionDisplay.setPortionLink(portionLink);
				}

				sectionDisplay.setRange(section.getRange());
				double percent = setupPageRange(holdingDisplay, sectionDisplay, section);

				int status = copyrightWebService.getStatus(context, portionItem, attachment.getUuid());
				if( status == ActivateRequest.TYPE_ACTIVE )
				{
					sectionDisplay.setStatus(LABEL_STATUS_ACTIVE);
					holdingDisplay.setTotalActivePercent(percent + holdingDisplay.getTotalActivePercent());
				}
				else
				{
					Label statusLabel = status == ActivateRequest.TYPE_INACTIVE ? LABEL_STATUS_INACTIVE
						: LABEL_STATUS_PENDING;
					sectionDisplay.setStatus(statusLabel);
					holdingDisplay.setTotalInactivePercent(percent + holdingDisplay.getTotalInactivePercent());
				}
				holdingDisplay.setTotalPercent(percent + holdingDisplay.getTotalPercent());

				if( canActivate && (session == null || session.isSelectAttachments()) && !restricted )
				{
					HtmlComponentState activateButton = new HtmlComponentState();
					boolean inIntegration = integrating && session != null;
					activateButton.setLabel(inIntegration ? LABEL_ACTIVATE_ANDADD : LABEL_ACTIVATE);
					activateButton.setClickHandler(events.getNamedHandler("activateOne", //$NON-NLS-1$
						jsonAttachId));
					ButtonRenderer activateOneButton = new ButtonRenderer(activateButton).addClass("activate-one");
					if( inIntegration )
					{
						activateOneButton.showAs(ButtonType.ADD).addClass("button-expandable expand-left");
					}
					sectionDisplay.setActivateButton(activateOneButton);

					if( (session == null || session.isSelectMultiple()) && (portion.getSections().size() > 1
						|| (holdingIsThis && portion.getHolding().getPortions().size() > 1)) )
					{
						sectionDisplay.setCheckBox(selections.getBooleanState(context, jsonAttachId));
						holdingDisplay.setHasCheckboxes(true);
					}
					holdingDisplay.setHaveActivate(true);
				}

				if( session != null && (status != ActivateRequest.TYPE_INACTIVE || !integrating) )
				{
					String courseCode = session.getStructure()
						.getAttribute(StructuredIntegrationSessionExtension.KEY_COURSE_CODE);
					if( courseCode == null && integrating )
					{
						courseCode = integrationService.getIntegrationInterface(context).getCourseInfoCode();
					}
					boolean canAdd = !integrating || activationService
						.attachmentIsSelectableForCourse(copyrightService.getActivationType(), attachUuid, courseCode);
					if( canAdd )
					{
						HtmlComponentState addState = new HtmlComponentState();
						addState.setClickHandler(
							events.getNamedHandler("addAttachment", portionItem.getItemId(), attachUuid));
						sectionDisplay
							.setAddButton(new ButtonRenderer(addState).showAs(ButtonType.PLUS).addClass("add"));
						holdingDisplay.setHaveAdd(true);
					}
				}

				sections.add(sectionDisplay);
			}
		}
		portionDisplay.setSections(sections);

	}

	//Dirty hack, mostly copy and pasted from AbstractActivateSection
	@EventHandlerMethod
	public void addAttachment(SectionInfo info, ItemId itemId, String attachmentId)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			final Item item = itemService.getUnsecure(itemId);

			final boolean integrating = integrationService.isInIntegrationSession(info);
			final IAttachment attachment;
			final ActivateRequest activateRequest;
			if (integrating)
			{
				final String courseCode = getCourseCode(info, session, integrationService.isInIntegrationSession(info));
				if (activationService
						.attachmentIsSelectableForCourse(copyrightService.getActivationType(), attachmentId, courseCode))
				{
					final List<ActivateRequest> requests = activationService
							.getAllCurrentAndPendingActivations(getCopyrightServiceImpl().getActivationType(), attachmentId);

					//FIXME: we need some logic to pull out the best activate request in the case there is > 1
					// Pull out the first activate request with matching course if there is one, otherwise just whatever is first.
					activateRequest = requests.stream().sorted((o1, o2) ->
						{
							boolean firstMatch = o1.getCourse().getCode().equals(courseCode);
							boolean secondMatch = o2.getCourse().getCode().equals(courseCode);
							return -Boolean.compare(firstMatch, secondMatch);
						}).findFirst().get();
					attachment = copyrightWebService.getAttachmentMap(info, item)
							.get(activateRequest.getAttachment());
				}
				else
				{
					throw new RuntimeException("Attachment " + attachmentId + " is not selectable for the current course.");
				}
			}
			else
			{
				activateRequest = null;
				attachment = new UnmodifiableAttachments(item).getAttachmentByUuid(attachmentId);
			}

			addResource(info, item, attachment, activateRequest);
		}
	}

	private void addResource(SectionInfo info, Item item, IAttachment attachment, @Nullable ActivateRequest activateRequest)
	{
		final CourseListSection cls = info.lookupSection(CourseListSection.class);
		if( cls != null && cls.isApplicable(info) )
		{
			for( String folder : cls.getSelectedFolders(info) )
			{
				addResource(info, new SelectedResource(item.getItemId(), attachment,
						selectionService.findTargetFolder(info, folder), null), activateRequest);
			}
		}
		else
		{
			addResource(info, new SelectedResource(item.getItemId(), attachment, null, null), activateRequest);
		}
	}

	private void addResource(SectionInfo info, SelectedResource resource, @Nullable ActivateRequest activateRequest)
	{
		if (activateRequest != null)
		{
			resource.addExtender(new ViewRequestUrl(activateRequest.getUuid()));
			resultsExtension.addRequest(resource, activateRequest);
		}
		selectionService.addSelectedResource(info, resource, true);
	}

	private String getCourseCode(SectionInfo info, SelectionSession session, boolean integrating)
	{
		String courseCode = session.getStructure().getAttribute("courseCode");
		if( courseCode == null && integrating )
		{
			courseCode = integrationService.getIntegrationInterface(info).getCourseInfoCode();
		}
		return courseCode;
	}

	protected abstract void processAvailablePages(Holding holding, HoldingDisplay holdingDisplay);

	protected TextLabel getAttachmentDisplayName(Attachment attachment)
	{
		return new TextLabel(attachment.getDescription());
	}

	protected double setupPageRange(HoldingDisplay holdingDisplay, SectionDisplay sectionDisplay, Section section)
	{
		double percent = 0;
		int pages = PageCounter.countTotalRange(section.getRange());
		sectionDisplay.setPages(pages);
		if( holdingDisplay.getTotalPages() > 0 )
		{
			percent = (pages / (double) holdingDisplay.getTotalPages()) * 100;
			sectionDisplay.setPercent(percent);
		}
		return percent;
	}

	private HtmlLinkState getViewItemCommand(RenderEventContext context, Item item, @Nullable Attachment attachment)
	{
		HtmlLinkState command = new HtmlLinkState();
		ViewItemUrl vurl = urlFactory.createItemUrl(context, item.getItemId());
		if( attachment != null )
		{
			vurl.add(new ViewAttachmentUrl(attachment.getUuid()));
		}
		command.setBookmark(vurl);
		return command;
	}

	@EventHandlerMethod
	public void activateOne(SectionInfo info, String attachmentId)
	{
		activateSection.doActivate(info, new String[]{attachmentId});
	}

	@EventHandlerMethod
	public void activateAll(SectionInfo info)
	{
		Set<String> checkedSet = selections.getCheckedSet(info);
		if( !checkedSet.isEmpty() )
		{
			activateSection.doActivate(info, checkedSet.toArray(new String[checkedSet.size()]));
			selections.clearChecked(info);
		}
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectionService.addSelectedResource(info,
			selectionService.createAttachmentSelection(info, itemId, attachment, null, extensionType), false);
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		if( isVisible(info) )
		{
			event.setHandler(this);
		}
	}

	public boolean isVisible(SectionInfo info)
	{
		final Model model = getModel(info);

		if( model.isVisible() == null )
		{
			boolean visible = false;

			final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
			final Item item = itemInfo.getItem();
			final ViewableItem<Item> viewableItem = itemInfo.getViewableItem();

			if( viewableItem.getPrivileges().contains("VIEW_ITEM") )
			{
				if( copyrightService.isCopyrightedItem(item) && viewableItem.isItemForReal() )
				{
					Holding holding = copyrightWebService.getHolding(info, item);
					if( holding != null )
					{
						visible = true;
						model.setItemHolding(holding);
					}
				}
			}
			model.setVisible(visible);
		}
		return model.isVisible();
	}

	private void sortPortions(HoldingDisplay holdingDisplay, List<PortionDisplay> portions,
		List<LinkRenderer> otherPortions)
	{
		if( holdingDisplay.isBook() )
		{
			Collections.sort(portions, new NumberStringComparator<PortionDisplay>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(PortionDisplay t)
				{
					if( t.getChapter() != null )
					{
						return t.getChapter();
					}
					return t.getTitle().getText();
				}
			});
		}

		Collections.sort(otherPortions, new NumberStringComparator<LinkRenderer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(LinkRenderer t)
			{
				return t.getLabelText();
			}
		});
	}

	public Button getActivateSelected()
	{
		return activateSelected;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@NonNullByDefault(false)
	public static class Model
	{
		private Boolean visible;
		private String[] selected;
		private String attachmentId;
		private Holding itemHolding;
		private HoldingDisplay holding;
		private boolean showPortionLinks;

		public Boolean isVisible()
		{
			return visible;
		}

		public void setVisible(boolean visible)
		{
			this.visible = visible;
		}

		public HoldingDisplay getHolding()
		{
			return holding;
		}

		public void setHolding(HoldingDisplay holding)
		{
			this.holding = holding;
		}

		public String[] getSelected()
		{
			return selected;
		}

		public void setSelected(String[] selected)
		{
			this.selected = selected;
		}

		public String getAttachmentId()
		{
			return attachmentId;
		}

		public void setAttachmentId(String attachmentId)
		{
			this.attachmentId = attachmentId;
		}

		public Holding getItemHolding()
		{
			return itemHolding;
		}

		public void setItemHolding(Holding itemHolding)
		{
			this.itemHolding = itemHolding;
		}

		public boolean isShowPortionLinks()
		{
			return showPortionLinks;
		}

		public void setShowPortionLinks(boolean showPortionLinks)
		{
			this.showPortionLinks = showPortionLinks;
		}
	}

	@NonNullByDefault(false)
	public static class PortionDisplay
	{
		private Label title;
		private String chapter;
		private boolean thisItem;
		private List<SectionDisplay> sections = new ArrayList<SectionDisplay>();

		public List<SectionDisplay> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionDisplay> sections)
		{
			this.sections = sections;
		}

		public String getChapter()
		{
			return chapter;
		}

		public void setChapter(String chapter)
		{
			this.chapter = chapter;
		}

		public boolean isThisItem()
		{
			return thisItem;
		}

		public void setThisItem(boolean thisItem)
		{
			this.thisItem = thisItem;
		}

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}
	}

	public static class HoldingDisplay
	{
		private boolean book;
		private boolean showPages;
		private SectionResult holdingLink;
		private int totalPages;
		private double totalPercent;
		private double totalActivePercent;
		private double totalInactivePercent;
		private boolean haveActivate;
		private boolean haveAdd;
		private boolean holdingDisplay;
		private boolean hasCheckboxes;
		private List<PortionDisplay> portions;
		private List<? extends SectionResult> otherPortions;
		private int pagesAvailable;

		public List<? extends SectionResult> getOtherPortions()
		{
			return otherPortions;
		}

		public void setOtherPortions(List<? extends SectionResult> otherPortions)
		{
			this.otherPortions = otherPortions;
		}

		public List<PortionDisplay> getPortions()
		{
			return portions;
		}

		public void setPortions(List<PortionDisplay> portions)
		{
			this.portions = portions;
		}

		public int getTotalPages()
		{
			return totalPages;
		}

		public void setTotalPages(int totalPages)
		{
			this.totalPages = totalPages;
		}

		public boolean isBook()
		{
			return book;
		}

		public void setBook(boolean book)
		{
			this.book = book;
		}

		public double getTotalPercent()
		{
			return totalPercent;
		}

		public void setTotalPercent(double totalPercent)
		{
			this.totalPercent = totalPercent;
		}

		public double getTotalActivePercent()
		{
			return totalActivePercent;
		}

		public void setTotalActivePercent(double totalActivePercent)
		{
			this.totalActivePercent = totalActivePercent;
		}

		public double getTotalInactivePercent()
		{
			return totalInactivePercent;
		}

		public void setTotalInactivePercent(double totalInactivePercent)
		{
			this.totalInactivePercent = totalInactivePercent;
		}

		public SectionResult getHoldingLink()
		{
			return holdingLink;
		}

		public void setHoldingLink(SectionResult holdingLink)
		{
			this.holdingLink = holdingLink;
		}

		public boolean isHaveActivate()
		{
			return haveActivate;
		}

		public void setHaveActivate(boolean haveActivate)
		{
			this.haveActivate = haveActivate;
		}

		public boolean isHoldingDisplay()
		{
			return holdingDisplay;
		}

		public void setHoldingDisplay(boolean holdingDisplay)
		{
			this.holdingDisplay = holdingDisplay;
		}

		public boolean isShowPages()
		{
			return showPages;
		}

		public void setShowPages(boolean showPages)
		{
			this.showPages = showPages;
		}

		public int getPagesAvailable()
		{
			return pagesAvailable;
		}

		public void setPagesAvailable(int pagesAvailable)
		{
			this.pagesAvailable = pagesAvailable;
		}

		public boolean isHaveAdd()
		{
			return haveAdd;
		}

		public void setHaveAdd(boolean haveAdd)
		{
			this.haveAdd = haveAdd;
		}

		public boolean isHasCheckboxes()
		{
			return hasCheckboxes;
		}

		public void setHasCheckboxes(boolean hasCheckboxes)
		{
			this.hasCheckboxes = hasCheckboxes;
		}
	}

	public static class SectionDisplay
	{
		private double percent;
		private String range;
		private Label status;
		private int pages;
		private HtmlBooleanState checkBox;
		private ButtonRenderer activateButton;
		private ImageRenderer icon;
		private LinkTagRenderer viewLink;
		private HtmlLinkState portionLink;
		private ButtonRenderer addButton;

		public String getRange()
		{
			return range;
		}

		public void setRange(String range)
		{
			this.range = range;
		}

		public int getPages()
		{
			return pages;
		}

		public void setPages(int pages)
		{
			this.pages = pages;
		}

		public double getPercent()
		{
			return percent;
		}

		public void setPercent(double percent)
		{
			this.percent = percent;
		}

		public HtmlBooleanState getCheckBox()
		{
			return checkBox;
		}

		public void setCheckBox(HtmlBooleanState checkBox)
		{
			this.checkBox = checkBox;
		}

		public Label getStatus()
		{
			return status;
		}

		public void setStatus(Label status)
		{
			this.status = status;
		}

		public ButtonRenderer getActivateButton()
		{
			return activateButton;
		}

		public void setActivateButton(ButtonRenderer activateButton)
		{
			this.activateButton = activateButton;
		}

		public HtmlLinkState getPortionLink()
		{
			return portionLink;
		}

		public void setPortionLink(HtmlLinkState portionLink)
		{
			this.portionLink = portionLink;
		}

		public ImageRenderer getIcon()
		{
			return icon;
		}

		public void setIcon(ImageRenderer icon)
		{
			this.icon = icon;
		}

		public LinkTagRenderer getViewLink()
		{
			return viewLink;
		}

		public void setViewLink(LinkTagRenderer viewLink)
		{
			this.viewLink = viewLink;
		}

		public ButtonRenderer getAddButton()
		{
			return addButton;
		}

		public void setAddButton(ButtonRenderer addButton)
		{
			this.addButton = addButton;
		}

	}

	public static class AttachmentId
	{
		private long id;
		private String attachmentId;

		public AttachmentId()
		{
			super();
		}

		public AttachmentId(long id, String attachmentId)
		{
			this.id = id;
			this.attachmentId = attachmentId;
		}

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getAttachmentId()
		{
			return attachmentId;
		}

		public void setAttachmentId(String attachmentId)
		{
			this.attachmentId = attachmentId;
		}

		public String toJSONString()
		{
			return JSONObject.fromObject(this).toString();
		}
	}
}