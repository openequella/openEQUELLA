<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl">
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="flickr.css" hasRtl=true />

<@div class="filter checkbox search-layout" id="creative-commons-filter">
	<h3>${b.key("filter.creativeCommonsOnly")}</h3>
	<div class="input radio">
		<@checklist section=s.licenceList list=true />
	</div>
</@div>
<hr />
