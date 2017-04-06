/**
 * General purpose EQUELLA SOAP services. History 4.1 - New interface
 * {@link com.tle.web.remoting.soap.SoapService41} has been added. <h1><a
 * name="xmlFormats">XML formats</a></h1> When invoking SOAP methods, XML
 * representations of objects are often accepted or returned. These objects have
 * a specific format and are guaranteed not to change (except for additions) in
 * future releases. The XML formats for the various objects are described below.
 * <p>
 * Key:
 * <ul>
 * <li>| - a seperator for a list of possible values</li>
 * <li>[attribute="x"] - an attribute which may or may not be present</li>
 * <li>... - any number of repetitions of the previous node type</li>
 * </ul>
 * <h3><a name="itemXmlFormat">Item XML format</a></h3> Used in creation and
 * editing of an item, also in search results. <div class="block">
 * 
 * <pre>
 * &lt;item id=&quot;itemUuid&quot; version=&quot;itemVersion&quot; itemdefid=&quot;collectionUuid&quot; itemstatus=&quot;live|moderating|draft&quot;&gt;
 *    &lt;name&gt;Item Name&lt;/name&gt;
 *    &lt;description&gt;Item Description&lt;/description&gt;
 *    &lt;url&gt;http://myhost/myinst/itemUuid/itemVersion/&lt;/url&gt;
 *    &lt;owner&gt;User ID&lt;/owner&gt;
 *    &lt;datecreated&gt;ISO Date&lt;/datecreated&gt;
 *    &lt;datemodified&gt;ISO Date&lt;/datemodified&gt;
 *    &lt;rating average=&quot;1&quot;&gt;&lt;/rating&gt;
 *    &lt;newitem&gt;true|false&lt;/newitem&gt;
 *    &lt;staging&gt;Staging area ID&lt;/staging&gt;
 *    
 *    &lt;!-- The attachments on the item, as seen on the item summary screen --&gt;
 *    &lt;attachments&gt;
 *       &lt;attachment type=&quot;local|remote|plan&quot;&gt;
 *          &lt;uuid&gt;Attachment ID&lt;/uuid&gt;
 *          &lt;file&gt;&lt;/file&gt;
 *          &lt;description&gt;&lt;/description&gt;
 *          &lt;conversion&gt;&lt;/conversion&gt;
 *          &lt;size&gt;&lt;/size&gt;
 *          &lt;thumbnail&gt;&lt;/thumbnail&gt;
 *       &lt;/attachment&gt;
 *       ...
 *    &lt;/attachments&gt;
 *    
 *    &lt;!-- Any URLs on the item that cannot be visited --&gt;
 *    &lt;badurls&gt;
 *       &lt;url message=&quot;&quot; status=&quot;HTTP status code&quot; tries=&quot;&quot; url=&quot;&quot; /&gt;
 *       ...
 *    &lt;/badurls&gt;
 *    
 *    &lt;!-- Item moderation history --&gt;
 *    &lt;history&gt;
 *       &lt;statechange|resetworkflow|approved|edit|promoted|comment|rejected 
 *             applies=&quot;false&quot; date=&quot;ISO Date&quot; state=&quot;&quot; user=&quot;User ID&quot; [comment=&quot;&quot;]
 *             [step=&quot;&quot;] [stepName=&quot;&quot;] [tostep=&quot;&quot;] [toStepName=&quot;&quot;]&gt;
 *          User ID
 *       &lt;/statechange|resetworkflow|approved|edit|promoted|comment|rejected&gt;
 *       ...
 *    &lt;/history&gt;
 *    
 *    &lt;!-- IMS package style viewer navigation --&gt;
 *    &lt;navigationNodes&gt;
 *       &lt;node&gt;
 *          &lt;uuid&gt;Node ID&lt;/uuid&gt;
 *          &lt;name&gt;Node Name&lt;/name&gt;
 *          &lt;tab attachment=&quot;Attachment ID&quot; viewer=&quot;|livNavTreeViewer&quot;&gt;
 *             &lt;name&gt;Tab Name&lt;/name&gt;
 *          &lt;/tab&gt;
 *          ...
 *       &lt;/node&gt;
 *       ...
 *    &lt;/navigationNodes&gt;
 *    
 *    &lt;!-- Digital rights management --&gt;
 *    &lt;rights&gt;
 *       &lt;offer&gt;
 *          &lt;party&gt;
 *             &lt;context [owner=&quot;true&quot;]&gt;
 *                &lt;name&gt;Party name&lt;/name&gt;
 *                &lt;remark&gt;Party email&lt;/remark&gt;
 *                &lt;uid&gt;&quot;tle:&quot; + User id&lt;/uid&gt;
 *             &lt;/context&gt;
 *          &lt;/party&gt;
 *          ...
 *          
 *          &lt;permission&gt;
 *             &lt;requirement&gt;
 *                &lt;attribution/&gt;
 *                &lt;accept&gt;
 *                   &lt;context&gt;
 *                      &lt;remark&gt;Terms of agreement&lt;/remark&gt;
 *                   &lt;/context&gt;
 *                &lt;/accept&gt;
 *             &lt;/requirement&gt;
 *             
 *             &lt;container&gt;
 *                &lt;constraint&gt;
 *                   &lt;!-- If restricted to sector --&gt;
 *                   &lt;purpose type=&quot;sectors:educational&quot; /&gt;
 *                   
 *                   &lt;!-- If restricted to groups or individuals --&gt;
 *                   &lt;individual|group&gt;
 *                      &lt;context&gt;
 *                         &lt;uid&gt;&quot;tle:&quot; + user or group ID&lt;/uid&gt;
 *                      &lt;/context&gt;
 *                   &lt;/individual|group&gt;
 *                   ...
 *                   
 *                   &lt;!-- If maximum usage count --&gt;
 *                   &lt;count&gt;&lt;/count&gt;
 *                   
 *                   &lt;!-- If restricted to IP ranges --&gt;
 *                   &lt;network name=&quot;Display name for range&quot;&gt;
 *                      &lt;range&gt;
 *                         &lt;min&gt;&lt;/min&gt;
 *                         &lt;max&gt;&lt;/max&gt;
 *                      &lt;/range&gt;
 *                   &lt;/network&gt;
 *                   
 *                   &lt;!-- If restriced by date range --&gt;
 *                   &lt;datetime&gt;
 *                      &lt;start&gt;ISO date midnight (yyyy-MM-dd&quot;T00:00:00&quot;)&lt;/start&gt;
 *                      &lt;end&gt;ISO date midnight (yyyy-MM-dd&quot;T00:00:00&quot;)&lt;/end&gt;
 *                   &lt;/datetime&gt;
 *                &lt;/constraint&gt;
 *             &lt;/container&gt;
 *             
 *             &lt;!-- The presence of any of the nodes below indicates a true value --&gt;
 *             &lt;tle_ownerMustAccept/&gt;
 *             &lt;tle_preview/&gt;
 *             &lt;tle_attributionIsEnforced/&gt;
 *             &lt;tle_licenceCount/&gt;
 *             &lt;tle_summary/&gt;
 *             &lt;tle_hideLicencesFromOwner/&gt;
 *             &lt;tle_attributionIsEnforced/&gt;
 *             &lt;tle_showLicenceInComposition/&gt;
 *          &lt;/permission&gt;
 *       &lt;/offer&gt;
 *    &lt;/rights&gt;
 *    
 * &lt;/item&gt;
 * </pre>
 * 
 * </div>
 * <h3><a name="collectionXmlFormat">Collection XML format</a></h3>
 * <em>Note: Collections are often referred to internally as Item Definitions or Item Defs.</em>
 * <div class="block">
 * 
 * <pre>
 * &lt;itemdef&gt;
 *    &lt;uuid&gt;Collection ID&lt;/uuid&gt;
 *    &lt;name&gt;Collection Name&lt;/name&gt;
 *    &lt;system&gt;true|false&lt;/system&gt;
 *    &lt;type&gt;&lt;/type&gt;
 *    &lt;embeddedtemplate&gt;XSLT file&lt;embeddedtemplate&gt;
 *    &lt;schemaUuid&gt;The unique ID of the schema used by this collection&lt;/schemaUuid&gt;
 * &lt;/itemdef&gt;
 * </pre>
 * 
 * </div>
 * <h3><a name="schemaXmlFormat">Schema XML format</a></h3> <div class="block">
 * 
 * <pre>
 * &lt;schema&gt;
 *    &lt;uuid&gt;Schema ID&lt;/uuid&gt;
 *    &lt;name&gt;Schema Name&lt;/name&gt;
 *    &lt;description&gt;Schema Description&lt;/description&gt;
 *    &lt;itemNamePath&gt;xpath to use for storing item name&lt;/itemNamePath&gt;
 *    &lt;itemDescriptionPath&gt;xpath to use for storing item description&lt;/itemDescriptionPath&gt;
 * &lt;/schema&gt;
 * </pre>
 * 
 * </div>
 * <h3><a name="userXmlFormat">User XML format</a></h3> <div class="block">
 * 
 * <pre>
 * &lt;user&gt;
 *    &lt;uuid&gt;User ID&lt;/uuid&gt;
 *    &lt;username&gt;Login name&lt;/username&gt;
 *    &lt;firstName&gt;First Name&lt;/firstName&gt;
 *    &lt;lastName&gt;Last Name&lt;/lastName&gt;
 *    &lt;email&gt;Email Address&lt;/email&gt;
 * &lt;/user&gt;
 * </pre>
 * 
 * </div>
 * <h3><a name="taskXmlFormat">Task XML format</a></h3> <div class="block">
 * 
 * <pre>
 * &lt;task&gt;
 *    &lt;itemUuid&gt;Item ID&lt;/itemUuid&gt;
 *    &lt;itemVersion&gt;Item Version&lt;/itemUuid&gt;
 *    &lt;taskUuid&gt;Task ID&lt;/itemUuid&gt;
 * &lt;/task&gt;
 * </pre>
 * 
 * </div>
 * <h3><a name="taskFilterCountXmlFormat">Task Filter Count XML format</a></h3>
 * <div class="block">
 * 
 * <pre>
 * &lt;filter&gt;
 *    &lt;id&gt;Filter ID&lt;/id&gt;
 *    &lt;name&gt;Display name of the filter&lt;/name&gt;
 *    &lt;href&gt;URL that can be used to view the filter inside EQUELLA&lt;/href&gt;
 *    &lt;count&gt;The number of results for this filter&lt;/count&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * </div>
 */
package com.tle.web.remoting.soap;