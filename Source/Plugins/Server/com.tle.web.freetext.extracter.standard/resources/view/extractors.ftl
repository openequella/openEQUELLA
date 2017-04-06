<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@a.div id="extractors">
	<#if m.hasExtractors()>
		<h3>${b.key("mimefreetextedit.title")}</h3>
		<@render s.extractorsTable />
	</#if>
</@a.div>