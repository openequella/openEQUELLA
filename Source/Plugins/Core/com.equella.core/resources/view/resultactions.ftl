<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="resultactions.css" hasRtl=false />

<@div id="actionbuttons">
	<#list m.buttons as b>
		<@button section=b />
	</#list>
</@div>

<@div id="searchresults-actions">	
	<#if m.childSections??>
		<div id="actioncontent" class="resulttopblock">
			<@renderList m.childSections/>
		</div>
	</#if>
</@div>