<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div id="${id}popupbrowserControl" class="popupbrowser">
	<@render s.termsTable />
</@div>