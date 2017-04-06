package com.tle.web.sections.standard;

import com.tle.web.sections.SectionTree;

/**
 * A factory class for create components.
 * 
 * @author jmaginnis
 */
public interface ComponentFactory
{
	Button createButton(String parentId, String id, SectionTree tree);

	Link createLink(String parentId, String id, SectionTree tree);

	SingleSelectionList<?> createSingleSelectionList(String parentId, String id, SectionTree tree);

	MultiSelectionList<?> createMultiSelectionList(String parentId, String id, SectionTree tree);

	TextField createTextField(String parentId, String id, SectionTree tree);

	Checkbox createCheckbox(String parentId, String id, SectionTree tree);

	/**
	 * Assigns a unique ID to the component and calls tree.registerInnerSection
	 * 
	 * @param parentId
	 * @param id
	 * @param tree
	 * @param component
	 */
	void registerComponent(String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component);

	void setupComponent(String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component);

	<T extends AbstractHtmlComponent<?>> T createComponent(String parentId, String id, SectionTree tree,
		Class<T> clazz, boolean register);

}
