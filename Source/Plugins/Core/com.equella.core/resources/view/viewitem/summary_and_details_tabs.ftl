<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "summarydetailstabs.css" />

<@a.div id="summary-details-tabs">
	<div class="tab-area">
		<ul class="nav nav-tabs">
			<li id="summary_tab" <#if m.tabId?? && m.tabId=='summary'>class="active"</#if> >
				<@render s.summaryTabLink/>
			</li>
			<li id="details_tab" <#if m.tabId?? && m.tabId=='details'>class="active"</#if>>
				<@render s.detailsTabLink/>
			</li>
		</ul>
	</div>
</@a.div>
<div class="renderable_area area">
	<@render m.renderable/>
</div>