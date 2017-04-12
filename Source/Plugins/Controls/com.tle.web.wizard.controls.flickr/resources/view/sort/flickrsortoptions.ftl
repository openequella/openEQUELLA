<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<@css path="flickr.css" hasRtl=true />

<@a.div class="input" id="flickr-sort">
	<@render s.labelTag>
		<h4>${b.key("sort.order.title")}</h4>
	</@render>
	<div class="input select">
		<@render s.sortOptions />
	</div>
	<div class="input checkbox"> 
		<@render section=s.reverse />
	</div>
</@a.div>