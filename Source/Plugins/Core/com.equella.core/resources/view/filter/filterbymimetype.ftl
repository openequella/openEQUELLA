<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css "filter/filterbymimetype.css" />

<div class="filter filterbymimetype">
	<h3>${b.key("filter.bymimetype.title")}</h3>
	<div class="input checkbox">
		<@checklist section=s.mimeTypes list=true class="resourcetype" />
	</div>
</div>
<hr>