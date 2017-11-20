<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="filter">
	<@render s.labelTag><h3>${b.key("filter.bystatus.title")}</h3></@render>
	<div class="input select">
		<@render section=s.itemStatus />
	</div>
	<#if !m.hideCheckBox>
		<div class="input checkbox">
			<@render section=s.onlyInModeration/>
		</div>
	</#if>
</div>
<hr>