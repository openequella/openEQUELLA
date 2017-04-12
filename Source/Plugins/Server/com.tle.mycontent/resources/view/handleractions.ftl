<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<#if m.majorActions??>
	<#list m.majorActions as action>
		<@render action />
	</#list>
</#if>

<#if m.minorActions??>
	<div class="minoractions">
		<#list m.minorActions as action>
			<#if action_index gt 0><span>|</span></#if>
			<@render action />
		</#list>
	</div>
</#if>