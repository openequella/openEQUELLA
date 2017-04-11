package com.tle.web.sections;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.SectionEvent;

/**
 * The implementation of {@code SectionsController} is responsible for the flow
 * of control from the initial {@code HttpServletRequest} to the final writing
 * of the {@code HttpServletResponse}.
 * <p>
 * Here is how it goes:
 * <ol>
 * <li>Create a {@link SectionInfo} instance.</li>
 * <li>Lookup a {@link SectionTree} the path via the
 * {@link com.tle.web.sections.registry.TreeRegistry}</li>
 * <li>Add the tree to the {@code SectionInfo}</li>
 * <li>Queue all {@link SectionTree#getApplicationEvents()} events.
 * <li>Run all registered {@link SectionFilter}s</li>
 * <li>Create and process a {@link com.tle.web.sections.events.ParametersEvent}</li>
 * <li>Process all {@link SectionEvent}s under the priority of
 * {@link SectionEvent#PRIORITY_PRERENDER}</li>
 * <li>See if this request should be redirected for PRG</li>
 * <li>Process any outstanding {@code SectionEvent}s</li>
 * <li>If output hasn't been rendered yet, (e.g.
 * {@link SectionInfo#setRendered()} hasn't been called) send a
 * {@link com.tle.web.sections.events.RenderEvent} to the root id of the
 * {@code SectionInfo} using an
 * {@link com.tle.web.sections.render.OutputResultListener} to write the result
 * to the {@code HttpServletResponse}</li>
 * </ol>
 * 
 * @author jmaginnis
 */
@NonNullByDefault
public interface SectionsController extends InfoCreator
{
	/**
	 * Forward to a URL.
	 * <p>
	 * Sends a redirect, using {@code HttpServletResponse#sendRedirect(String)}.
	 * <br>
	 * Calls {@link SectionInfo#setRendered()} to prevent any further
	 * processing.
	 * 
	 * @param info The {@code SectionInfo}
	 * @param url The URL to forward to. Preferably a fully qualified URL,
	 *            because if it isn't, the {@code HttpServletResponse} will add
	 *            the host name etc, and probably get it wrong.
	 * @param code The status code to redirect with
	 */
	void forwardToUrl(SectionInfo info, String url, int code);

	/**
	 * Forwards to another {@code SectionInfo} (created via a call to
	 * {@link #createForward(SectionInfo, String)}). <br>
	 * Picks up from step 7 above.
	 * 
	 * @param original The originating {@code SectionInfo}
	 * @param forward The {@code SectionInfo} to forward to
	 */
	void forward(SectionInfo original, SectionInfo forward);

	void forwardAsBookmark(SectionInfo original, SectionInfo forward);

	/**
	 * Create a new {@code SectionInfo} for a given path.
	 * 
	 * @param info The current {@code SectionInfo}
	 * @param path The path to get a {@code SectionInfo} for
	 * @return The newly created {@code SectionInfo}
	 */
	SectionInfo createForward(SectionInfo info, String url);

	boolean treeExistsForUrlPath(String path);

	MutableSectionInfo createInfo(String path, @Nullable HttpServletRequest request,
		@Nullable HttpServletResponse response, @Nullable SectionInfo from, @Nullable Map<String, String[]> params,
		@Nullable Map<?, ?> attrs);

	MutableSectionInfo createInfo(SectionTree tree, String path, @Nullable HttpServletRequest request,
		@Nullable HttpServletResponse response, @Nullable SectionInfo from, @Nullable Map<String, String[]> params,
		@Nullable Map<?, ?> attrs);

	void execute(SectionInfo info);

	void handleException(SectionInfo info, Throwable t, @Nullable SectionEvent<?> event);

	MutableSectionInfo createInfoFromTree(SectionTree tree, SectionInfo info);
}
