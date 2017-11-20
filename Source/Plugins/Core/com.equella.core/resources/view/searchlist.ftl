<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl">

<@div id="searchfavouritesresults" class="searchfavouritesresults" tag=m.tag>	
	<#list m.items as item>
		<div class="itemresult">
			<div class="itemresult-content">
				<h3><@render item.title/></h3>
			</div>
		</div>
	</#list>
</@div>