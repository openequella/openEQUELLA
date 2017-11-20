<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css path="editmetadata.css" hasRtl=true />

<#-- prerender for the addNode()  function -->
<@render s.addNodeFunc />

<@a.div id="metadata-edit">
<#-- schema section -->
	<#if m.schemaSelection>
		<h3>${b.key("schema.title")}</h3>
		<div class="schema-selection">
			<@dropdown section=s.schemaList />
		</div>
		<@a.div id="selected-nodes">
			<@a.div id="treePanel">
				<@render section=s.schemaTree class="topic-tree treeview-gray" />
			</@a.div>
			<@textfield  section=s.pathValues class="term-paths" />		
		</@a.div>
		
<#-- action section -->
	<#elseif m.actionSelection>
		<h3>${b.key("action.title")}</h3>
		<@setting label=b.key("action.modify")>
			${m.nodeDisplay}
		</@setting>		
		<@setting label=b.key("action.actionlist") section=s.actionList />
		<@a.div id="action-form">
			<#if m.actionReplace>
				<@setting label=b.key("action.replace.find") section=s.findTextField />
				<@setting label=b.key("action.replace.replace") section=s.replaceTextField />
			<#elseif m.actionSet>
				<@setting label=b.key("action.set.text") section=s.setTextField />
				<@setting label=b.key("action.set.options")>
					<@checklist section=s.setTextOptions list=true/>
				</@setting>
			<#elseif m.actionAdd>					
				<@setting label=b.key("action.add.xml")>
					<@textarea section=s.addXMLTextArea rows=5 />		
				</@setting>
			</#if>
		</@a.div>
<#-- modification section -->
	<#else>
		<h3>${b.key("modifications.title")}</h3>
		<@render s.modsTable />
	</#if>
</@a.div>