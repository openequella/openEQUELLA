<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<@css "actions/sort.css" />

<div class="sortaction">
	<@render s.labelTag><h3>${b.key("sortsection.order.title")}</h3></@render>
	<div class="input select">
		<@render s.sortOptions />
	</div>
	<@a.div id="reverse" class="input checkbox"> 
		<@render section=s.reverse />
	</@a.div>
</div>
