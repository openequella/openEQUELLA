<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<@script "qtiplayviewer.js" />

<div class="question-body result-summary">
	<#if m.heading??>
		<h3>${m.heading}</h3>
	</#if>

	<#list m.feedbacks as feedback>
		<@render feedback />
	</#list>
</div>

