<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div id="filter_${id}" class="filter">
	<h3>${s.title}</h3>
	<div class="input select range">
		<@render s.list />
	</div>
</div>
<hr>