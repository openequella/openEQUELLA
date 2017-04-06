<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<div id="menu" class="${m.displayClass}" role="navigation">
	<#list m.contributions.keySet() as groupKey> 
		<ul>
			<#list m.contributions.get(groupKey) as link>
				<li><@render link.first /></li>
			</#list>				
		</ul>
	</#list>
</div>