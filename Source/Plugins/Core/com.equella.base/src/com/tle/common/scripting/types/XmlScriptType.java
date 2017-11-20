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

package com.tle.common.scripting.types;

import java.io.Serializable;
import java.util.List;

/**
 * Object for XML manipulation in scripts. Generally referred to as 'xml' in
 * scripts, also available as 'newxml' in New Version scripts and 'attributes'
 * in Advanced Scripting wizard controls. Note that all XPath parameters are not
 * <b>true</b> XPaths and are limited to simple node/attribute selection and
 * node indexes. E.g. /xml/test/node[2]/@attribute is as complex as it will get.
 */
public interface XmlScriptType extends Serializable
{
	/**
	 * Get the text value of a node using an XPath like syntax. If there is more
	 * than one node with that XPath, it will return the value in the first one.
	 * If the node cannot be found, a blank string will be returned. You can use
	 * exists(String) to determine if a node exists.
	 * 
	 * @param xpath The XPath to get the value from
	 * @return The value from the XML document
	 */
	String get(String xpath);

	/**
	 * Set the text value of a node. Node will be created if there is none found
	 * at XPath
	 * 
	 * @param xpath The XPath to the node.
	 * @param value The new text value of the node.
	 */
	void set(String xpath, String value);

	/**
	 * Find out if any value of the nodes with a given XPath match a certain
	 * value.
	 * 
	 * @param xpath The XPath to the node(s)
	 * @param value The value to check for
	 * @return true if the value is found
	 */
	boolean contains(String xpath, String value);

	/**
	 * Determine if any node exists at the following XPath.
	 * 
	 * @param xpath The XPath to the node
	 * @return true if any node can be found
	 */
	boolean exists(String xpath);

	/**
	 * Returns the number of matching XPaths.
	 * 
	 * @param xpath The XPath to the node(s).
	 * @return the number of matching XPaths.
	 */
	int count(String xpath);

	/**
	 * Returns all node text values for a given XPath.
	 * 
	 * @param xpath The XPath to the node(s)
	 * @return An array of all the text values of the matching nodes
	 */
	String[] getAll(String xpath);

	/**
	 * Same as getAll(String) but returns a java.util.List object
	 * 
	 * @param xpath The XPath to the node(s)
	 * @return A list of all the text values of the matching nodes
	 */
	List<String> list(String xpath);

	/**
	 * A more useful alternative to getAll.
	 * 
	 * @param xpath The XPath to the sub-tree root node(s)
	 * @return An array of XmlScriptType which are the sub-trees found at XPath
	 */
	XmlScriptType[] getAllSubtrees(String xpath);

	/**
	 * Add a new node and set its value. This will <b>always</b> add a new node,
	 * even if there is already a node at the given XPath.
	 * 
	 * @param xpath The XPath to the new node
	 * @param value The text value of the new node
	 */
	void add(String xpath, String value);

	/**
	 * Copy the text value of the node found at srcXpath into the text value of
	 * the node found at destXpath.
	 * 
	 * @param srcXpath The XPath to the source node
	 * @param destXpath The XPath to the destination node
	 */
	void copy(String srcXpath, String destXpath);

	/**
	 * Deletes the first node matching the XPath
	 * 
	 * @param xpath The XPath of the node to delete
	 */
	void deleteNode(String xpath);

	/**
	 * Deletes all nodes matching the XPath
	 * 
	 * @param xpath The XPath of the nodes to delete.
	 */
	void deleteAll(String xpath);

	/**
	 * Deletes all nodes and changes the XML to &lt;xml/&gt;
	 */
	void clear();

	/**
	 * Returns an XmlScriptType object for the sub-tree that is rooted at the
	 * first node matching XPath. If there is no node at XPath, then null is
	 * returned. This sub-tree is still linked to the original tree, any changes
	 * made to the sub-tree will be reflected in the original.
	 * 
	 * @param xpath The XPath of the sub-tree root node.
	 * @return The sub-tree found at XPath.
	 */
	XmlScriptType getSubtree(String xpath);

	/**
	 * Same as getSubtree(String) only that if there is no node matching XPath,
	 * a new sub-tree is created.
	 * 
	 * @param xpath The XPath of the sub-tree root node.
	 * @return The sub-tree found at XPath, or a brand new sub-tree rooted at
	 *         XPath.
	 */
	XmlScriptType getOrCreateSubtree(String xpath);

	/**
	 * Creates a subtree rooted at XPath. This will always create a subtree,
	 * regardless of whether there is a node at XPath already.
	 * 
	 * @param xpath The XPath of the sub-tree root node.
	 * @return The new sub-tree rooted at XPath.
	 */
	XmlScriptType createSubtree(String xpath);

	/**
	 * Removes the subtree from the xml.
	 * 
	 * @param subtree The tree to remove from the xml (obtained via getSubtree,
	 *            getOrCreateSubtree, createSubtree or getAllSubtrees)
	 */
	void deleteSubtree(XmlScriptType subtree);

	/**
	 * Appends another XML document at the given XPath.
	 * 
	 * @param xpath The XPath to insert the new document into.
	 * @param documentToAppend The XML document to insert.
	 */
	void append(String xpath, XmlScriptType documentToAppend);

	/**
	 * Appends the first-level of children from an XML document at the given
	 * XPath. This works the same as <code>append(String, XmlScriptType)</code>,
	 * but does not include the root element of the document.
	 * 
	 * @param xpath The XPath to insert the new document into.
	 * @param documentToAppend The XML document whose first-level children will
	 *            be inserted from.
	 */
	void appendChildren(String xpath, XmlScriptType documentToAppend);

	/**
	 * Returns an XmlScriptType object for the sub-tree that is rooted at the
	 * node with an "id" attribute value matching the parameter. If there is no
	 * node at XPath, then null is returned. This sub-tree is still linked to
	 * the original tree, any changes made to the sub-tree will be reflected in
	 * the original.
	 * 
	 * @param id The ID of the sub-tree root node.
	 * @return The sub-tree found for ID.
	 */
	XmlScriptType findForId(String id);

	/**
	 * @return A string representation of the XML
	 */
	String asString();
}
