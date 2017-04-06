package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeServer;
import com.tle.web.sections.standard.model.HtmlTreeState;
import com.tle.web.sections.standard.renderers.TreeRenderer;

@NonNullByDefault
public class Tree extends AbstractDisablerComponent<Tree.TreeModel> implements HtmlTreeServer
{
	private HtmlTreeModel model;
	@Nullable
	private TreeRenderer treeRenderer;
	private final SectionId displayTree = this;
	private boolean lazyLoad;
	private boolean allowMultipleOpenBranches;

	@AjaxFactory
	private AjaxGenerator ajaxMethods;
	private JSCallable ajaxFunction;

	public Tree()
	{
		super(RendererConstants.TREE);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		if( lazyLoad )
		{
			ajaxFunction = ajaxMethods.getAjaxFunction("getTreeNodes"); //$NON-NLS-1$			
		}
	}

	@Override
	public JSCallable getAjaxFunctionForNode(SectionInfo info, String nodeId)
	{
		return ajaxFunction;
	}

	@Override
	public Bookmark getAjaxUrlForNode(SectionInfo info, String nodeId)
	{
		return new BookmarkAndModify(info, ajaxMethods.getModifier("getTreeNodes", nodeId)); //$NON-NLS-1$
	}

	@AjaxMethod
	public JSONResponseCallback getTreeNodes(AjaxRenderContext context, String nodeId)
	{
		context.setModalId(displayTree.getSectionId());
		return getTreeRenderer(context).getJSONResponse();
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		super.prepareModel(info);
		HtmlTreeState state = getState(info);
		if( lazyLoad )
		{
			state.setTreeServer(this);
			state.setLazyLoad(true);
		}
		state.setModel(model);
		state.setAllowMultipleOpenBranches(allowMultipleOpenBranches);
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		TreeRenderer treeRend = (TreeRenderer) renderer;
		if( treeRenderer == null )
		{
			treeRenderer = treeRend;
		}
		getModel(info).setTreeRenderer(treeRend);
	}

	@Override
	protected SectionRenderable chooseRenderer(RenderContext info, TreeModel state)
	{
		TreeRenderer renderer;
		TreeModel treeModel = getModel(info);
		if( treeRenderer != null )
		{
			renderer = treeRenderer.createNewRenderer(state);
			state.fireRendererCallback(info, renderer);
		}
		else if( treeModel.isSelectingRenderer() )
		{
			renderer = (TreeRenderer) super.chooseRenderer(info, state);
			treeRenderer = renderer;
		}
		else
		{
			treeModel.setSelectingRenderer(true);
			renderSection(info, displayTree);
			return treeModel.getTreeRenderer();
		}
		treeModel.setTreeRenderer(renderer);
		return renderer;
	}

	public TreeRenderer getTreeRenderer(RenderContext info)
	{
		TreeModel treeModel = getModel(info);
		TreeRenderer renderer = treeModel.getTreeRenderer();
		if( renderer != null )
		{
			return renderer;
		}
		treeModel.setSelectingRenderer(true);
		renderSection(info, displayTree);
		return treeModel.getTreeRenderer();
	}

	@Override
	public Class<TreeModel> getModelClass()
	{
		return TreeModel.class;
	}

	public HtmlTreeModel getModel()
	{
		return model;
	}

	public void setModel(HtmlTreeModel model)
	{
		this.model = model;
	}

	public boolean isLazyLoad()
	{
		return lazyLoad;
	}

	public void setLazyLoad(boolean lazyLoad)
	{
		this.lazyLoad = lazyLoad;
	}

	public boolean isAllowMultipleOpenBranches()
	{
		return allowMultipleOpenBranches;
	}

	public void setAllowMultipleOpenBranches(boolean allowMultipleOpenBranches)
	{
		this.allowMultipleOpenBranches = allowMultipleOpenBranches;
	}

	@NonNullByDefault(false)
	public static class TreeModel extends HtmlTreeState
	{
		private TreeRenderer treeRenderer;
		private boolean selectingRenderer;

		@SuppressWarnings("unchecked")
		@Override
		public <T extends HtmlComponentState> Class<T> getClassForRendering()
		{
			return (Class<T>) HtmlTreeState.class;
		}

		public TreeRenderer getTreeRenderer()
		{
			return treeRenderer;
		}

		public void setTreeRenderer(TreeRenderer treeRenderer)
		{
			this.treeRenderer = treeRenderer;
		}

		public boolean isSelectingRenderer()
		{
			return selectingRenderer;
		}

		public void setSelectingRenderer(boolean selectingRenderer)
		{
			this.selectingRenderer = selectingRenderer;
		}
	}
}
