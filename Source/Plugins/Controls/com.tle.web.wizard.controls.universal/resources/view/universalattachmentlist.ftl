<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "universalresource.css" />

<@div id="${id}universalresources" class="universalresources">
	<@render s.attachmentsTable />
</@div>