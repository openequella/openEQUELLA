<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@a.div id="bulk-selection">
	<@css path="bulkresults.css" hasRtl=true />
	<@render s.div>
		<@render s.box>
			<h3>${m.selectionBoxCountLabel}</h3>
			<ul class="blue">
				<#if m.selections?size gt 0 || m.bitSet.size() gt 0>
					<li><@render s.viewSelectedLink /></li>
					<li><@render s.unselectAllLink /></li>
				</#if>
				<li><@render s.selectAllButton/></li>			
			</ul>
		</@render>
		<@render s.executeButton />
		<#-- TODO: make more generic -->
		<#if s.moderateSelectedButton?? >
			<@render s.moderateSelectedButton />
		</#if>
		<hr>
	</@render>
</@a.div>
