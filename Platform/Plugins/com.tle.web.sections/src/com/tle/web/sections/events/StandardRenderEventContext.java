package com.tle.web.sections.events;

import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.PathGenerator;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

public class StandardRenderEventContext implements RenderEventContext
{
	private RenderContext renderContext;
	private final SectionId sectionId;
	private final RenderEvent event;

	public StandardRenderEventContext(SectionId sectionId, RenderContext renderContext, RenderEvent event)
	{
		this.sectionId = sectionId;
		if( renderContext instanceof StandardRenderEventContext )
		{
			this.renderContext = ((StandardRenderEventContext) renderContext).renderContext;
		}
		else
		{
			this.renderContext = renderContext;
		}
		this.event = event;
	}

	@Override
	public PreRenderContext getPreRenderContext()
	{
		return renderContext.getPreRenderContext();
	}

	@Override
	public void addEventFilter(SectionEventFilter filter)
	{
		renderContext.addEventFilter(filter);
	}

	@Override
	public void clearModel(SectionId id)
	{
		renderContext.clearModel(id);
	}

	@Override
	public boolean containsId(String id)
	{
		return renderContext.containsId(id);
	}

	@Override
	public SectionInfo createForward(String path, Map<Object, Object> attributes)
	{
		return renderContext.createForward(path, attributes);
	}

	@Override
	public SectionInfo createForward(String path)
	{
		return renderContext.createForward(path);
	}

	@Override
	public void forward(SectionInfo forward)
	{
		renderContext.forward(forward);
	}

	@Override
	public void forwardAsBookmark(SectionInfo forward)
	{
		renderContext.forwardAsBookmark(forward);
	}

	@Override
	public void forwardToUrl(String url, int code)
	{
		renderContext.forwardToUrl(url, code);
	}

	@Override
	public void forwardToUrl(String url)
	{
		renderContext.forwardToUrl(url);
	}

	@Override
	public List<SectionId> getAllChildIds(SectionId id)
	{
		return renderContext.getAllChildIds(id);
	}

	@Override
	public <T> T getAttribute(Object name)
	{
		return renderContext.<T>getAttribute(name);
	}

	@Override
	public <T> T getAttributeForClass(Class<T> clazz)
	{
		return renderContext.<T>getAttributeForClass(clazz);
	}

	@Override
	public <T extends SectionId, S extends T> S lookupSection(Class<T> clazz)
	{
		return renderContext.lookupSection(clazz);
	}

	@Override
	public <T extends SectionId, S extends T> List<S> lookupSections(Class<T> clazz)
	{
		return renderContext.lookupSections(clazz);
	}

	@Override
	public <T> T getAttributeSafe(Object name, Class<?> clazz)
	{
		return renderContext.<T>getAttributeSafe(name, clazz);
	}

	@Override
	public Bookmark getPublicBookmark()
	{
		return renderContext.getPublicBookmark();
	}

	@Override
	public boolean getBooleanAttribute(Object name)
	{
		return renderContext.getBooleanAttribute(name);
	}

	@Override
	public List<SectionId> getChildIds(SectionId id)
	{
		return renderContext.getChildIds(id);
	}

	@Override
	@Deprecated
	public SectionContext getContextForId(String id)
	{
		return renderContext.getContextForId(id);
	}

	@Override
	public <T> T getLayout(String id)
	{
		return renderContext.<T>getLayout(id);
	}

	@Override
	public void removeListeners(String target)
	{
		renderContext.removeListeners(target);
	}

	@Override
	public String getModalId()
	{
		return renderContext.getModalId();
	}

	@Override
	public <T> T getModelForId(String id)
	{
		return renderContext.<T>getModelForId(id);
	}

	@Override
	public RenderContext getRootRenderContext()
	{
		return renderContext.getRootRenderContext();
	}

	@Override
	public SectionResult getRenderedBody()
	{
		return renderContext.getRenderedBody();
	}

	@Override
	public SectionRenderable getRenderedResponse()
	{
		return renderContext.getRenderedResponse();
	}

	@Override
	public HttpServletRequest getRequest()
	{
		return renderContext.getRequest();
	}

	@Override
	public HttpServletResponse getResponse()
	{
		return renderContext.getResponse();
	}

	@Override
	public String getRootId()
	{
		return renderContext.getRootId();
	}

	@Override
	public RenderResultListener getRootResultListener()
	{
		return renderContext.getRootResultListener();
	}

	@Override
	public SectionId getSectionForId(SectionId id)
	{
		return renderContext.getSectionForId(id);
	}

	@Override
	public String getSemiModalId()
	{
		return renderContext.getSemiModalId();
	}

	@Override
	public <T> T getTreeAttribute(Object key)
	{
		return renderContext.<T>getTreeAttribute(key);
	}

	// @Override
	// public <T> T getTreeAttributeForClass(Class<T> clazz)
	// {
	// return renderContext.getTreeAttributeForClass(clazz);
	// }

	@Override
	public <T> T getTreeData(SectionId id)
	{
		return renderContext.<T>getTreeData(id);
	}

	@Override
	public <T> List<T> getTreeListAttribute(Object key)
	{
		return renderContext.<T>getTreeListAttribute(key);
	}

	@Override
	public String getWrappedRootId(SectionId id)
	{
		return renderContext.getWrappedRootId(id);
	}

	@Override
	public boolean isRendered()
	{
		return renderContext.isRendered();
	}

	@Override
	public void preventGET()
	{
		renderContext.preventGET();
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event, SectionTree tree)
	{
		renderContext.processEvent(event, tree);
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event)
	{
		renderContext.processEvent(event);
	}

	@Override
	public <L extends EventListener> void queueEvent(SectionEvent<L> event)
	{
		renderContext.queueEvent(event);
	}

	@Override
	public void queueTreeEvents(SectionTree tree)
	{
		renderContext.queueTreeEvents(tree);
	}

	@Override
	public void renderNow()
	{
		renderContext.renderNow();
	}

	@Override
	public void setAttribute(Object name, Object attribute)
	{
		renderContext.setAttribute(name, attribute);
	}

	@Override
	public void setModalId(String modalId)
	{
		renderContext.setModalId(modalId);
	}

	@Override
	public void setRendered()
	{
		renderContext.setRendered();
	}

	@Override
	public void setRenderedBody(SectionResult renderedBody)
	{
		renderContext.setRenderedBody(renderedBody);
	}

	@Override
	public void setRenderedResponse(SectionRenderable renderedResponse)
	{
		renderContext.setRenderedResponse(renderedResponse);
	}

	@Override
	public void setRootResultListener(RenderResultListener rootResultListener)
	{
		renderContext.setRootResultListener(rootResultListener);
	}

	@Override
	public void setSemiModalId(String semiModalId)
	{
		renderContext.setSemiModalId(semiModalId);
	}

	@Override
	public String getSectionId()
	{
		return sectionId.getSectionId();
	}

	@Override
	public Section getSectionObject()
	{
		return sectionId.getSectionObject();
	}

	@Override
	public SectionTree getTree()
	{
		return sectionId.getTree();
	}

	@Override
	public RenderEvent getRenderEvent()
	{
		return event;
	}

	@Override
	@Deprecated
	public SectionInfo getInfo()
	{
		return renderContext;
	}

	@Override
	@Deprecated
	public SectionId getSection()
	{
		return renderContext.getSectionForId(sectionId);
	}

	@Override
	public HeaderHelper getHelper()
	{
		return renderContext.getHelper();
	}

	@Override
	public void preRender(Collection<? extends PreRenderable> preRenderers)
	{
		renderContext.preRender(preRenderers);
	}

	@Override
	public void preRender(PreRenderable preRenderer)
	{
		renderContext.preRender(preRenderer);
	}

	@Override
	public void preRender(PreRenderable... preRenderers)
	{
		renderContext.preRender(preRenderers);
	}

	@Override
	public void setRootRenderContext(RenderContext renderContext)
	{
		this.renderContext.setRootRenderContext(renderContext);
	}

	@Override
	public boolean isRenderHeader()
	{
		return renderContext.isRenderHeader();
	}

	@Override
	public BodyTag getBody()
	{
		return renderContext.getBody();
	}

	@Override
	public FormTag getForm()
	{
		return renderContext.getForm();
	}

	@Override
	public boolean isForceRender()
	{
		return renderContext.isForceRender();
	}

	@Override
	public void forceRedirect()
	{
		renderContext.forceRedirect();
	}

	@Override
	public boolean isForceRedirect()
	{
		return renderContext.isForceRedirect();
	}

	@Override
	public PathGenerator getPathGenerator()
	{
		return renderContext.getPathGenerator();
	}

	@Override
	public boolean isReal()
	{
		return renderContext.isReal();
	}

	@Override
	public boolean isErrored()
	{
		return renderContext.isErrored();
	}

	@Override
	public void setErrored()
	{
		renderContext.setErrored();
	}
}
