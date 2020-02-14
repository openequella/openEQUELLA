<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@div tag=m.divToggle class="toggler">
	<a title="${m.linkLabel}" href="#" aria-controls="${m.linkedItemId}" aria-expanded="${m.expanded?c}">
		<i class="${m.iconClass}"> </i>
	</a>
</@div>
