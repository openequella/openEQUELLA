<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@render s.saveButton />
<#if m.internal == true>
	<@render s.changePassButton />
</#if>


