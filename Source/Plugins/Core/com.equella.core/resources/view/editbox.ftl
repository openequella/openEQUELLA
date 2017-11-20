<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>

<div class="input text">
	<#if c.size2 lt 2>
		<@textfield section=s.field maxlength=1000/>
	<#else>
		<@textarea section=s.field rows=c.size2 class="ctrlEditboxMultiLine"/>
	</#if>
</div>