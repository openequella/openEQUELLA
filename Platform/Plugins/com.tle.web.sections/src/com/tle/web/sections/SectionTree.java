package com.tle.web.sections;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.registry.TreeRegistry;

/**
 * The <code>SectionTree</code> is an object which stores a tree of
 * {@link Section}s.
 * <p>
 * Normally a <code>SectionTree</code> will be mapped to a URL (e.g.
 * <code>/access/search.do</code>) and registered with the {@link TreeRegistry},
 * but in the case of {@link SectionFilter}s they generally create anonymous
 * <code>SectionTree</code>s.
 * <p>
 * Generally a Section will only deal with the <code>SectionTree</code> directly
 * during the tree registration process. Normally that will consist of
 * registering any child/inner <code>Section</code>s and registering any
 * application {@link SectionEvent}s and/or {@link EventListener}s
 * 
 * @author jmaginnis
 */
public interface SectionTree
{
	/**
	 * Check for the existance of a particular Section id.
	 * 
	 * @param id The id to look for
	 * @return True if it exists
	 */
	boolean containsId(String id);

	/**
	 * A place holder id is a more descriptive id for a section. <br>
	 * Generally it's used as an id for an extension point where other sections
	 * can be inserted.
	 * 
	 * @param placeHolderId
	 * @return The Section id of that placeHolderId
	 */
	String getPlaceHolder(String placeHolderId);

	/**
	 * Get the parent id of the section with the given id.
	 * 
	 * @param sectionId The id of the section to find the parent of
	 * @return The parent id
	 */
	String getParentId(String sectionId);

	/**
	 * Get the Section for the given id.
	 * 
	 * @param sectionId The id of the Section
	 * @return The Section
	 */
	Section getSectionForId(String sectionId);

	/**
	 * Get a list of child id's for the given parent id. Does not include inner
	 * children. Note that this is used within the Sections framework for
	 * renderFirstResult and renderChildren
	 * 
	 * @param sectionId
	 * @return A list of child Section id's
	 */
	List<SectionId> getChildIds(String sectionId);

	/**
	 * Get a list of all child id's (including inner children) for the given
	 * parent id.
	 * 
	 * @param sectionId
	 * @return A list of child Section id's
	 */
	List<SectionId> getAllChildIds(String sectionId);

	/**
	 * Register an inner Section. <br>
	 * An inner Section is a section which will not be returned by the
	 * <code>getChildIds()</code> call (which is used within the Sections
	 * framework for renderFirstResult and renderChildren).<br>
	 * Generally a section should be registered as an inner section if the
	 * parent has a hard dependency on it, e.g. components from the
	 * <code>com.tle.web.sections.standard</code> package.
	 * 
	 * @param section The Section to register
	 * @param parentId The id of the parent section
	 * @return The id of the registered section
	 */
	String registerInnerSection(Object section, String parentId);

	/**
	 * Register one of more sections into the given parent id. <br>
	 * Same as calling
	 * <code>registerSections(section, parent, null, true)</code>
	 * 
	 * @param section Can be either a Section, or a {@link SectionNode}, or a
	 *            List of either.
	 * @param parent The id of the parent Section
	 * @return If only one Section is registered, the id of that Section.
	 * @see SectionNode
	 * @see #registerSections(Object, String, String, boolean)
	 */
	String registerSections(Object section, String parent);

	/**
	 * Register one of more sections into the given parent id, inserted after or
	 * before the given id. <br>
	 * 
	 * @param section Can be either a Section, or a {@link SectionNode}, or a
	 *            List of either.
	 * @param parent The id of the parent Section
	 * @param insertId The id of a sibling Section from which to insert after or
	 *            before. If <code>null</code> is passed in, always add the
	 *            Section's to the end of the parent.
	 * @param after True if Section's are to be inserted after the inserId, else
	 *            insert before.
	 * @return If only one Section is registered, the id of that Section.
	 * @see SectionNode
	 */
	String registerSections(Object section, String parent, String insertId, boolean after);

	/**
	 * Set an attribute on this SectionTree.
	 * 
	 * @param key Key to set
	 * @param value Value to set
	 */
	void setAttribute(Object key, Object value);

	/**
	 * Returns whether or not this SectionTree has an attribute with the given
	 * key.
	 * 
	 * @param key The Key to check
	 * @return True if the attribute exists
	 */
	boolean containsKey(Object key);

	/**
	 * Get an attribute on this SectionTree
	 * 
	 * @param <T> The type of the attribute
	 * @param key The key for the attribute
	 * @return The attribute for the given key
	 */
	<T> T getAttribute(Object key);

	/**
	 * Finds the section 'closest' to reference. By closest I mean: 1. Search
	 * children first. 2. Check parent and it's children (excluding reference of
	 * course) next 3. Check the parent's parent and it's children etc. Until we
	 * find a match.
	 * 
	 * @param key
	 * @param reference
	 * @return
	 */
	<T extends SectionId, S extends T> S lookupSection(Class<T> key, SectionId reference);

	/**
	 * Finds all sections indexed with key
	 * 
	 * @param key
	 * @return
	 */
	<T extends SectionId, S extends T> List<S> lookupSections(Class<T> key);

	/**
	 * Set private SectionTree related data for a particular Section.
	 * 
	 * @param <T> The type of the data
	 * @param id The id of the Section
	 * @param data The data to set
	 */
	<T> void setData(String id, T data);

	/**
	 * Get private data previously set with <code>setData()</code>
	 * 
	 * @param <T> The type of the data
	 * @param id The id of the Section
	 * @return The data for the Section
	 * @see #setData(String, Object)
	 */
	<T> T getData(String id);

	/**
	 * Set the layout information for a particular section. The format of this
	 * data can be anything, and it usually up to the parent as to what makes
	 * sense here.
	 * 
	 * @param <T> The type of the layout data
	 * @param id The id of the Section
	 * @return The layout data object or null if non set
	 */
	<T> T getLayout(String id);

	/**
	 * @param id
	 * @param data
	 */
	void setLayout(String id, Object data);

	/**
	 * Get the root id of this SectionTree. <br>
	 * All Section's registered within this SectionTree are prepended with this
	 * id.<br>
	 * A {@link SectionInfo} can only contain SectionTree's with different root
	 * id's.
	 * 
	 * @return The root id
	 */
	String getRootId();

	/**
	 * Get all application events associated with this SectionTree.
	 * 
	 * @return All application {@link SectionEvent}s
	 */
	List<SectionEvent<? extends EventListener>> getApplicationEvents();

	/**
	 * Add an application {@link SectionEvent}. <br>
	 * Any events registered with the SectionTree will be queued in the
	 * {@link SectionInfo} by the {@link SectionsController}.
	 * 
	 * @param event The SectionEvent to be added.
	 */
	void addApplicationEvent(SectionEvent<? extends EventListener> event);

	/**
	 * Get any <code>EventListener</code>s registered to the given target.
	 * 
	 * @param <T> The type of the EventListener subclass
	 * @param target The target event listener list. Use <code>null</code> for
	 *            the broadcast event listener list.
	 * @param clazz The EventListener subclass
	 * @return A list of event listeners, which can either by a
	 *         <code>String</code> which is the id of a Section which implements
	 *         the EventListener subclass, or an object that implements the
	 *         EventListener subclass.
	 */
	<T extends EventListener> List<Object> getListeners(String target, Class<? extends T> clazz);

	/**
	 * Register an <code>EventListener</code> in the given target list.
	 * 
	 * @param <T> The type of the EventListener subclass
	 * @param target The target event listener list. Use <code>null</code> for
	 *            the broadcast event listener list.
	 * @param clazz The EventListener subclass
	 * @param eventListener Can either by a <code>String</code> which is the id
	 *            of a Section which implements the EventListener subclass, or
	 *            an object that implements the EventListener subclass.
	 */
	<T extends EventListener> void addListener(String target, Class<T> clazz, Object eventListener);

	/**
	 * Used for pruning a section tree dynamically. E.g. a portlet has been
	 * removed from the tree.
	 * 
	 * @param target
	 */
	void removeListeners(String target);

	List<RegistrationHandler> getExtraRegistrationHandlers();

	void addRegistrationHandler(RegistrationHandler handler);

	void addDelayedRegistration(DelayedRegistration delayed);

	void runDelayedRegistration();

	void finished();

	boolean isFinished();

	void setRuntimeAttribute(Object key, Object value);

	Map<Object, Object> getRuntimeAttributes();

	<T> List<T> addToListAttribute(Object key, T value);

	interface DelayedRegistration
	{
		void register(SectionTree tree);
	}

	String getSubId(String parentId, String childId);

	String registerSubInnerSection(Section section, String parentId);

	String registerSubInnerSection(Section section, String parentId, String preferredId);
}
