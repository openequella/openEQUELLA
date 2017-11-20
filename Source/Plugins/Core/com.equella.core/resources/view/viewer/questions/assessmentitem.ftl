<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<div class="question-body">
	<#if m.errors??>
	<#list m.errors as error>
		<label for="${error.second}"><div class="alert alert-danger">${error.first}</div></label>
	</#list>
	</#if>
	
	<@render m.itemBody />
	
	<#list m.modalFeedbacks as fb>
		<@render fb />
	</#list>
</div>