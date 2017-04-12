<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl">

<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<div>
	<div class="ctrlbody">
		<h3 class="ctrltitle"><@bundlekey "pages.label" /></h3>
	<@ajax.div id ="pages-table-ajax">
		<@render s.pagesTable />
	</@ajax.div>	
		<#if !m.noAdd>
			<@render section=s.addPageLink class="add focus" />
		</#if>
		<#if m.scrapbook??>
			<@render section=m.scrapbook class="scrapbooklink add focus" />
		</#if>
	</div>
</div>