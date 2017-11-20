<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "history.css"/>

<h2>${b.key("summary.content.history.title")}</h2>
<@button section=s.backButton showAs="prev" />

<@ajax.div id="historyevents" tag=s.eventsTable>
	<@render s.historyTable />
</@ajax.div>

<#if m.reviewDate??>
<div class="history-review">
	<span><@bundlekey "summary.content.history.label.reviewdue." + m.reviewTense /> <@render section=m.reviewDate /></span>
</div>
</#if>

<div class="history-options">
	<@checklist section=s.detailsSelection class="input radiolist"/>
</div>