<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "browseby.css"/>

<#macro donode node>
	<#if !node.hide>
		<li>
			<@render node.viewLink>${node.title}</@render>
			<#if node.count gt 0>(${node.count})</#if>
			<#if node.children??>
				<ul>
				<#list node.children as row>
					<@donode row/>
				</#list>
				</ul>
			</#if>
		</li>
	<#else>
		<#list node.children as row>
			<@donode row/>
		</#list>
	</#if>
</#macro>

<#if m.rootDisplayRow.children??>
	<div id="matrix-results" class="area">
		<h4>${b.key('pleaseselect')}</h4>
		<ul id="matrix-tree">
			<@donode m.rootDisplayRow/>
		</ul>
	</div>
</#if>
