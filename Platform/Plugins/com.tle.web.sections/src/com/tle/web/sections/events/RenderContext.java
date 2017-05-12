package com.tle.web.sections.events;

import java.util.Collection;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author jmaginnis
 */
@NonNullByDefault
public interface RenderContext extends SectionInfo
{
	/**
	 * The "Modal-id" is the id of a Section which the Root Template section
	 * will render.
	 * 
	 * @return
	 */
	String getModalId();

	void setModalId(String modalId);

	/**
	 * The "Semi-Modal id" is an application specific Section id, which will
	 * generally be used by the Root Application Section, in order to wrap a
	 * "Modal" operation with other markup.
	 * 
	 * @return The Semi-Modal id
	 */
	String getSemiModalId();

	void setSemiModalId(String semiModalId);

	SectionResult getRenderedBody();

	void setRenderedBody(SectionResult renderedBody);

	RenderResultListener getRootResultListener();

	void setRootResultListener(RenderResultListener rootResultListener);

	/**
	 * Get the html result.
	 * <p>
	 * 
	 * <pre>
	 * HTTP/1.1 200
	 * 
	 * ${result}
	 * </pre>
	 * 
	 * @return
	 */
	SectionRenderable getRenderedResponse();

	void setRenderedResponse(SectionRenderable renderedResponse);

	HeaderHelper getHelper();

	boolean isRenderHeader();

	FormTag getForm();

	BodyTag getBody();

	@Deprecated
	PreRenderContext getPreRenderContext();

	@Deprecated
	void preRender(Collection<? extends PreRenderable> preRenderers);

	@Deprecated
	void preRender(PreRenderable preRenderer);

	@Deprecated
	void preRender(PreRenderable... preRenderers);

}
