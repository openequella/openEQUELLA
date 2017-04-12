package com.tle.mypages.mypagesviewer.section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.core.guice.Bind;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.treeviewer.AbstractTreeViewerModel;
import com.tle.web.viewitem.treeviewer.AbstractTreeViewerSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.attachments.ItemNavigationService;
import com.tle.web.viewurl.attachments.ItemNavigationService.NodeAddedCallback;

/**
 * Need only be used in the event that the item in question has page
 * attachments, but no navigation tree of it's own. This class will whip up a
 * temporary tree on each page request. Usage: point to the URL
 * /item/xxxxxxx/vv/viewpages.jsp
 * 
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class MyPagesViewerSection extends AbstractTreeViewerSection<AbstractTreeViewerModel>
{
	@Inject
	private MyPagesService myPagesService;
	@Inject
	private ItemNavigationService itemNavService;

	@Override
	protected void prepareTitle(SectionInfo info, Link title, BundleLabel itemName, ViewItemResource resource)
	{
		title.setLabel(info, itemName);
	}

	@Override
	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info)
	{
		AbstractTreeViewerModel model = getModel(info);
		ViewItemResource resource = model.getResource();
		Item item = (Item) resource.getViewableItem().getItem();

		List<ItemNavigationNode> nodes = new ArrayList<ItemNavigationNode>();
		itemNavService.populateTreeNavigationFromAttachments(item, nodes,
			myPagesService.getPageAttachments(info, null, item.getItemId().toString()), new NodeAddedCallback()
			{
				@Override
				public void execute(int index, ItemNavigationNode node)
				{
					// we have to fake up a reproducable UUID as this node is
					// never persisted to the database
					node.setUuid("uid" + index);
				}
			});

		return nodes;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		// No singular attachment to return
		return null;
	}

	@Override
	protected void registerPathMappings(RootItemFileSection rootSection)
	{
		rootSection.addViewerMapping(Type.FULL, this, MyPagesConstants.VIEW_PAGES);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "myvw";
	}

	@Override
	public Class<AbstractTreeViewerModel> getModelClass()
	{
		return AbstractTreeViewerModel.class;
	}
}
