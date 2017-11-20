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

package com.tle.web.hierarchy.soap;

public interface HierarchySoapInterface
{
	/**
	 * Returns the Hierarchy topic with the UUID specified by topicUuid
	 * 
	 * @param topicUuid the unique ID of the Hierarchy topic to be retrieved
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;topic uuid="abc123"&gt;
	 *     &lt;name&gt;Test1&lt;/name&gt;
	 *     &lt;short.description&gt;short description&lt;/short.description&gt;
	 *     &lt;long.description&gt;long description&lt;/long.description&gt;
	 *     &lt;subtopics.heading&gt;subtopics&lt;/subtopics.heading&gt;
	 *     &lt;results.heading&gt;resultsheadings&lt;/results.heading&gt;
	 *     &lt;hide.empty.subtopics&gt;{true|false}&lt;/hide.empty.subtopics&gt;
	 *     &lt;advanced.search&gt;uuid&lt;/advanced.search&gt;
	 *     &lt;display.items&gt;{true|false}&lt;/display.items&gt;
	 *     &lt;constraints&gt;
	 *          &lt;freetext&gt;freetextquery&lt;/freetext&gt;
	 *          &lt;schemata&gt;
	 *               &lt;schema uuid="abc123"&gt;/xml/item/name = "sometext"&lt;/schema&gt;
	 *               ...
	 *          &lt;/schemata&gt;
	 *          &lt;collections&gt;
	 *               &lt;collection uuid="abc123" /&gt;
	 *               ...
	 *          &lt;/collection&gt;
	 *     &lt;/constraints&gt;
	 *     &lt;inherited.constraints&gt;
	 *          &lt;inherit.freetext&gt;{true|false}&lt;/inherit.freetext&gt;
	 *          &lt;schemata&gt;
	 *               &lt;schema uuid="abc123"&gt;/xml/item/name = "hello world"&lt;/schema&gt;
	 *               ...
	 *          &lt;/schemata&gt;
	 *          &lt;collections&gt;
	 *               &lt;collection uuid="abc123" /&gt;
	 *               ...
	 *          &lt;/collection&gt;
	 *     &lt;/inherited.constraints&gt;
	 *     &lt;dynamic.filtering&gt;
	 *          &lt;xpath&gt;...&lt;/xpath&gt;
	 *          &lt;source&gt;{contributed|manual}&lt;/source&gt;
	 *     &lt;/dynamic.filtering&gt;
	 *     &lt;attributes&gt;
	 *          &lt;attribute key="abc123"&gt;
	 *               someattributetext
	 *          &lt;/attribute&gt;
	 *          ...
	 *     &lt;/attributes&gt;
	 *     &lt;keyresources&gt;
	 *          &lt;keyresource uuid="abc123" version="#" /&gt;
	 *          ...
	 *     &lt;/keyresources&gt;
	 * &lt;/topic&gt;
	 * </pre>
	 * 
	 *         </div>
	 */
	String getTopic(String topicUuid);

	/**
	 * Returns all topics of a parent Hierarchy topic specified by parentUuid.
	 * If no parentUuid is specified, all root level topics will be listed
	 * 
	 * @param parentUuid The unique ID of the Hierarchy topic for which the
	 *            children should be listed
	 * @return XML of format: <div class="block">
	 * 
	 *         <pre>
	 * &lt;topics&gt;
	 *     &lt;topic&gt; <em>See the getTopic() method for the format of the topic xml</em>
	 *     ...
	 * &lt;topics&gt;
	 */
	String listTopics(String parentUuid);

	/**
	 * Deletes the Hierarchy topic specified by topicUuid. This will also delete
	 * all child topics of the topic being deleted
	 * 
	 * @param topicUuid The unique ID of the Hierarchy topic to be deleted
	 */
	void deleteTopic(String topicUuid);

	/**
	 * Allows the creation of a Hierarchy topic with the parent being the topic
	 * specified by parentUuid. If no parent UUID is specified the topic shall
	 * be created as a root level topic.
	 * 
	 * @param parentUuid The unique ID of the parent Hierarchy topic
	 * @param topicXml The XML of the topic to be created -
	 *            <em>See the getTopic() method for the format of the topic xml</em>
	 * @param index The index the topic should be inserted into in relation to
	 *            its siblings. Zero inserts it as the first sibling, one as the
	 *            second sibling, and so on. If the index is less than zero or
	 *            greater than the number of siblings minus one, then the topic
	 *            will be added as the last sibling.
	 * @return The unique ID of the newly created Hierarchy topic
	 */
	String createTopic(String parentUuid, String topicXml, int index);

	/**
	 * Updates the Hierarchy topic specified by topicUuid with the information
	 * included in topicXml.
	 * 
	 * @param topicUuid The unique ID of the Hierarchy topic to be edited
	 * @param topicXml The XML from which the Hierarchy topic should be updated
	 *            -
	 *            <em>See the getTopic() method for the format of the topic xml</em>
	 */
	void editTopic(String topicUuid, String topicXml);

	/**
	 * Moves the Hierarchy topic specified by childId to the location specified
	 * by index within the parent Hierarchy topic specified by parentUuid. The
	 * only time parentUuid may be omitted is when moving root level Hierarchy
	 * topics
	 * 
	 * @param childId The unique ID of the Hierarchy topic to move
	 * @param parentUuid The unique ID of the parent Hierarchy topic
	 * @param index The location that the child Hierarchy topic should be moved
	 *            to in relation to the other children
	 */
	void moveTopic(String childId, String parentUuid, int index);
}
