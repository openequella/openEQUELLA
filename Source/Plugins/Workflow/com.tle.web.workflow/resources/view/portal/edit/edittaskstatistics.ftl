<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css path="edittaskstatistics.css" hasRtl=true />

<@setting label=b.key('portal.taskstats.editor.trend') labelFor=s.trend>
	<div class="trend">
		<@checklist section=s.trend class="input radio"/>
	</div>
</@setting>