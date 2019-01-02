/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.SectionEventFilter;

/**
 * This is the request time instances by which {@link Section}s communicate with
 * each other.
 * <p>
 * It could be considered analogous to {@link HttpServletRequest} but for the
 * Sections library. The responsibilities of the {@code SectionInfo} include:
 * <ul>
 * <li>Maintaining a list of {@link SectionTree}s, which can be thought of as a
 * wrapping list, with the last {@code SectionTree} in the list wrapping the
 * previous one.</li>
 * <li>Keeping a queue of {@link SectionEvent}s and ensuring that
 * {@code SectionEvent}s are processed by their appropriate listeners (see
 * {@link #processEvent(SectionEvent)})</li>
 * <li>Maintaining a cache of Model instances for any {@code Section}s contained
 * in the list of {@code SectionTree}s.</li>
 * </ul>
 * Instances of {@code SectionInfo} are created by the
 * {@link SectionsController}. To see how a {@code HttpServletRequest} gets
 * processed and ends up with a response written to the
 * {@code HttpServletResponse}, see the {@link SectionsController}.
 * 
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface SectionInfo extends InfoCreator
{
	String KEY_FROM_REQUEST = "$FROM_REQUEST$";
	String KEY_FOR_URLS_ONLY = "$FORURLS$";
	String KEY_FORWARDFROM = "$FORWARD_FROM$";
	String KEY_PATH = "$PATH$";
	String KEY_BASE_HREF = "$BASE_HREF$";
	String KEY_ORIGINALINFO = "$ORIGINAL_INFO$";
	// The original exception that was thrown
	String KEY_ORIGINAL_EXCEPTION = "$ORIGINAL_EXCEPTION$";
	// The exception cause matching the exception handler
	String KEY_MATCHED_EXCEPTION = "$MATCHED_EXCEPTION$";
	String KEY_MINIFIED = "$USE_MINIFIED$";

	/**
	 * Gets the cached Model instance for the given {@code Section} id.
	 * 
	 * @param <T> The type of the model
	 * @param id The id of the {@code Section} to get the Model instance for
	 * @return The Model instance for the given {@code Section} id
	 * @see Section#getModelClass()
	 * @see Section#instantiateModel(SectionInfo)
	 */
	<T> T getModelForId(String id);

	/**
	 * Gets the data registered with the {@code SectionTree} for the given
	 * {@code SectionId}
	 * 
	 * @param <T> The type of the tree data
	 * @param id The {@code SectionId} containing the id of the {@code Section}
	 *            to lookup data for
	 * @return The data from the {@code SectionTree}
	 * @see SectionTree#getData(String)
	 */
	@Nullable
	<T> T getTreeData(SectionId id);

	/**
	 * Check whether or not any registered {@code SectionTree}s contain a
	 * {@code Section} registered with the given id
	 * 
	 * @param id The id to look for
	 * @return true if the id is found
	 * @see SectionTree#containsId(String)
	 */
	boolean containsId(String id);

	/**
	 * Get the root id of the topmost {@code SectionTree}
	 * 
	 * @return The root id of the topmost {@code SectionTree}
	 */
	String getRootId();

	/**
	 * Get the root id of the next {@code SectionTree} in the wrapping list.
	 * 
	 * @param id The {@code SectionId} of a {@code Section} in the outer
	 *            {@code SectionTree}
	 * @return The root id of the wrapped {@code SectionTree}
	 */
	String getWrappedRootId(SectionId id);

	/**
	 * Get an attribute.
	 * 
	 * @param <T> The type of the attribute
	 * @param name The key for the attribute
	 * @return The attribute
	 */
	@Nullable
	<T> T getAttribute(Object name);

	/**
	 * Get a boolean attribute.
	 * 
	 * @param name The key for the attribute
	 * @return The attribute
	 */
	boolean getBooleanAttribute(Object name);

	/**
	 * Get an attribute, safely. If the attribute doesn't exist yet, it creates
	 * an instance using the Class passed in.
	 * 
	 * @param <T> The type of the attribute
	 * @param name The key for the attribute
	 * @param clazz The Class to call {@code newInstance()} on if the attribute
	 *            is null.
	 * @return The attribute
	 */
	<T> T getAttributeSafe(Object name, Class<?> clazz);

	/**
	 * Same as {@link #getAttribute(Object)} but more type safe.
	 * 
	 * @param <T> The type of the attribute
	 * @param clazz The class of the attribute
	 * @return The attribute which will be of type T
	 */
	@Nullable
	<T> T getAttributeForClass(Class<T> clazz);

	/**
	 * Set an attribute.
	 * 
	 * @param name The key of the attribute
	 * @param attribute The attribute to set
	 */
	void setAttribute(Object name, @Nullable Object attribute);

	/**
	 * Get an attribute from the {@code SectionTree} list.
	 * 
	 * @param <T> The type of the attribute
	 * @param key The key for the attribute
	 * @return The attribute
	 */
	@Nullable
	<T> T getTreeAttribute(Object key);

	/**
	 * Get a list which is a contains values from all {@code SectionTree}s
	 * combined. If a tree has the attribute defined, it must be a list.
	 * 
	 * @param <T> The type of attributes in the list
	 * @param key The key for the attribute
	 * @return The list of attributes
	 * @see SectionTree#addToListAttribute(Object, Object)
	 */
	<T> List<T> getTreeListAttribute(Object key);

	/**
	 * Same as {@link #getTreeAttribute(Object)} but more type safe.
	 * 
	 * @param <T> The type of the attribute
	 * @param clazz The class of the attribute
	 * @return The attribute
	 */
	// <T> T getTreeAttributeForClass(Class<T> clazz);

	/**
	 * Scans trees for any section registered as clazz OR if specific class not
	 * found then the first section which is a subclass of clazz
	 * 
	 * @param clazz
	 * @return
	 */
	@Nullable
	<T extends SectionId, S extends T> S lookupSection(Class<T> clazz);

	/**
	 * Scans trees for all sections indexed as clazz
	 * 
	 * @param clazz
	 * @return
	 */
	@Nullable
	<T extends SectionId, S extends T> List<S> lookupSections(Class<T> clazz);

	/**
	 * Get a {@link SectionContext} for a given id.
	 * 
	 * @param id The id of a registered {@code Section}.
	 * @return The context representing the passed in id.
	 */
	@Deprecated
	SectionContext getContextForId(String id);

	/**
	 * Get a list of child id's, without inner children.
	 * 
	 * @param id The {@code SectionId} to get child id's for
	 * @return A list of child id's, without inner children.
	 */
	List<SectionId> getChildIds(SectionId id);

	/**
	 * Get a list of child id's, including inner children.
	 * 
	 * @param id The {@code SectionId} to get child id's for
	 * @return A list of child id's, including inner children.
	 */
	List<SectionId> getAllChildIds(SectionId id);

	/**
	 * Get the {@code HttpServletResponse} associated with this
	 * {@code SectionInfo}
	 * 
	 * @return The response associated with this {@code SectionInfo}
	 */
	@Nullable
	HttpServletResponse getResponse();

	/**
	 * Get the {@code HttpServletRequest} associated with this
	 * {@code SectionInfo}
	 * 
	 * @return The request associated with this {@code SectionInfo}
	 */
	@Nullable
	HttpServletRequest getRequest();

	/**
	 * Removes all listeners for a particular target.
	 * 
	 * @param target The id of the target
	 */
	void removeListeners(String target);

	/**
	 * Queue a {@code SectionEvent} in the priority queue.
	 * 
	 * @param event The event to queue
	 */
	<L extends EventListener> void queueEvent(SectionEvent<L> event);

	/**
	 * Process an event, passing it to all listeners. <br>
	 * This involves either getting the listener list via
	 * {@link #getListeners(String, Class)} and calling
	 * {@link SectionEvent#fire(SectionId, SectionInfo, EventListener)} for each
	 * listener, or in the case of Direct events calling
	 * {@link SectionEvent#fireDirect(SectionId, SectionInfo)}.
	 * 
	 * @param event The event to process
	 */
	<L extends EventListener> void processEvent(SectionEvent<L> event);

	/**
	 * Process an event, passing it to all given listeners. <br>
	 * 
	 * @param event
	 * @param listeners
	 */
	<L extends EventListener> void processEvent(SectionEvent<L> event, SectionTree tree);

	/**
	 * Queue all the events for a particular {@code SectionTree}. <br>
	 * This should really only be called by the {@code SectionsController} or a
	 * {@link SectionFilter}.
	 * 
	 * @param tree The tree from which to get {@code SectionEvent}s from
	 * @see SectionTree#getApplicationEvents()
	 */
	void queueTreeEvents(SectionTree tree);

	/**
	 * Has {@link #setRendered()} been called?
	 * 
	 * @return true if {@link #setRendered()} has been called
	 */
	boolean isRendered();

	/**
	 * Set the fact that this {@code SectionInfo} has been rendered, so the
	 * {@link SectionsController} doesn't need to proceed with the render
	 * process. Also clears the event queue.
	 */
	void setRendered();

	/**
	 * Removes any event's with priority higher than
	 * {@code SectionEvent#PRIORITY_RENDER}.
	 */
	void renderNow();

	/**
	 * @see SectionsController#forwardToUrl(SectionInfo, String, int)
	 * @param url
	 */
	void forwardToUrl(String url);

	/**
	 * @see SectionsController#forwardToUrl(SectionInfo, String, int)
	 * @param url
	 * @param code
	 */
	void forwardToUrl(String url, int code);

	/**
	 * @see SectionsController#forward(SectionInfo, SectionInfo)
	 * @param forward
	 */
	void forward(SectionInfo forward);

	/**
	 * @see SectionsController#forwardAsBookmark(SectionInfo, SectionInfo)
	 * @param forward
	 */
	void forwardAsBookmark(SectionInfo forward);

	/**
	 * @see SectionsController#createForward(SectionInfo, String)
	 * @param path
	 * @return The {@code SectionInfo} for the given path
	 */
	SectionInfo createForward(String path, Map<Object, Object> attributes);

	/**
	 * Add an event filter.
	 * 
	 * @param filter
	 */
	void addEventFilter(SectionEventFilter filter);

	/**
	 * Returns the "public" bookmark. Basically the url that will be displayed
	 * in the browser. When we do the "GET" of PRG, this is the url we will send
	 * back.
	 * 
	 * @return An Bookmark representing the current state of this
	 *         {@code SectionInfo}
	 */
	Bookmark getPublicBookmark();

	/**
	 * Removes the model for the section with the id of id
	 * 
	 * @param id
	 */
	void clearModel(SectionId id);

	void preventGET();

	void forceRedirect();

	boolean isForceRender();

	boolean isForceRedirect();

	RenderContext getRootRenderContext();

	void setRootRenderContext(RenderContext renderContext);

	<T> T getLayout(String id);

	@Nullable
	SectionId getSectionForId(SectionId id);

	boolean isReal();

	boolean isErrored();

	void setErrored();

	Map<String, String[]> getParameterMap();
}
