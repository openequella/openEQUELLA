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

package com.tle.web.sections.standard.renderers.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.libraries.JQueryTreeView;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.HtmlTreeServer;
import com.tle.web.sections.standard.model.HtmlTreeState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.TreeRenderer;

public class TreeViewRenderer extends AbstractComponentRenderer
    implements JSDisableable, TreeRenderer {
  private final HtmlTreeState treeState;

  public TreeViewRenderer(HtmlTreeState state) {
    super(state);
    this.treeState = state;
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    if (treeState.getNodeId() == null) {
      super.realRender(writer);
    }
  }

  @SuppressWarnings("nls")
  @Override
  public void preRender(PreRenderContext info) {
    if (treeState.getNodeId() == null) {
      final ObjectExpression oe = new ObjectExpression();

      oe.put("animated", "fast");

      if (!treeState.isAllowMultipleOpenBranches()) {
        oe.put("unique", Boolean.TRUE);
      }

      if (treeState.isLazyLoad()) {
        HtmlTreeServer treeServer = treeState.getTreeServer();
        Bookmark url = treeServer.getAjaxUrlForNode(info, "");
        oe.put("url", url.getHref());

        HtmlTreeModel model = treeState.getModel();
        ArrayExpression initial = new ArrayExpression();
        List<HtmlTreeNode> rootNodes = model.getChildNodes(info, null);
        for (HtmlTreeNode rootNode : rootNodes) {
          ObjectExpression initNode = new ObjectExpression();
          initNode.put("hasChildren", !rootNode.isLeaf());
          initNode.put("id", rootNode.getId());

          SectionRenderable renderer = rootNode.getRenderer();
          if (renderer == null) {
            renderer = new SpanRenderer(new LabelRenderer(rootNode.getLabel()));
          }
          info.preRender(renderer);
          final StringWriter outbuf = new StringWriter();
          try {
            renderer.realRender(new SectionWriter(outbuf, info));
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
          initNode.put("text", outbuf.toString());
          initial.add(initNode);
        }
        oe.put("initial", initial);
      }
      info.addReadyStatements(JQueryTreeView.treeView(treeState, oe));
    }
    super.preRender(info);
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    HtmlTreeModel model = treeState.getModel();
    if (!treeState.isLazyLoad()) {
      List<HtmlTreeNode> rootNodes = model.getChildNodes(writer, null);
      for (HtmlTreeNode node : rootNodes) {
        renderNode(writer, model, node);
      }
    }
  }

  @SuppressWarnings("nls")
  private void renderNode(SectionWriter writer, HtmlTreeModel model, HtmlTreeNode node)
      throws IOException {
    writer.writeTag("li");
    SectionRenderable renderer = node.getRenderer();
    if (renderer == null) {
      renderer = new SpanRenderer(new LabelRenderer(node.getLabel()));
    }
    writer.preRender(renderer);
    renderer.realRender(writer);

    if (!node.isLeaf()) {
      writer.writeTag("ul");
      List<HtmlTreeNode> childNodes = model.getChildNodes(writer, node.getId());
      for (HtmlTreeNode childNode : childNodes) {
        renderNode(writer, model, childNode);
      }
      writer.endTag("ul");
    }
    writer.endTag("li");
  }

  @Override
  protected String getTag() {
    return "ul"; //$NON-NLS-1$
  }

  @Override
  public TreeRenderer createNewRenderer(HtmlTreeState state) {
    return new TreeViewRenderer(state);
  }

  @Override
  @SuppressWarnings("nls")
  public JSONResponseCallback getJSONResponse() {
    return new JSONResponseCallback() {
      @Override
      public Object getResponseObject(AjaxRenderContext context) {
        HtmlTreeModel model = treeState.getModel();
        String nodeId = context.getRequest().getParameter("root");
        if (nodeId.equals("source")) {
          nodeId = "";
        }

        List<TreeViewerNode> viewerNodes = new ArrayList<TreeViewerNode>();
        List<HtmlTreeNode> nodes = model.getChildNodes(context, nodeId);
        for (HtmlTreeNode node : nodes) {
          TreeViewerNode viewerNode = new TreeViewerNode(context);
          SectionRenderable renderer = node.getRenderer();
          if (renderer == null) {
            renderer = new LabelRenderer(node.getLabel());
          }
          viewerNode.setRenderer(renderer);
          if (!node.isLeaf()) {
            viewerNode.setId(node.getId());
            viewerNode.setHasChildren(true);
          }
          viewerNodes.add(viewerNode);
        }
        return viewerNodes;
      }
    };
  }

  @Override
  public JSCallable createDisableFunction() {
    return new DefaultDisableFunction(this);
  }

  public static class TreeViewerNode {
    private transient SectionRenderable renderer;
    private boolean hasChildren;
    private String url;
    private String id;
    private List<TreeViewerNode> children;
    private final RenderContext info;

    public TreeViewerNode(RenderContext info) {
      this.info = info;
    }

    public String getText() {
      return SectionUtils.renderToString(info, renderer);
    }

    public boolean isHasChildren() {
      return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
      this.hasChildren = hasChildren;
    }

    public List<TreeViewerNode> getChildren() {
      return children;
    }

    public void setChildren(List<TreeViewerNode> children) {
      this.children = children;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setRenderer(SectionRenderable renderer) {
      this.renderer = renderer;
    }

    public boolean isExpanded() {
      return false;
    }
  }
}
