<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<h2>${b.key("summary.content.currentmoderation.title")}</h2>

<div>
	<span>${b.key('summary.content.currentmoderation.totaltime')} ${m.totalTime}</span>
</div>
<h3>${b.key("summary.content.currentmoderation.awaiting.title")}</h3>
<@render s.moderatorsTable />

<h3>${b.key("summary.content.currentmoderation.progress.title")}</h3>
<div class="flowchart-thumb">
	<@render s.flowchartLink>
		<@render m.flowchartThumb />
	</@render>
</div>
