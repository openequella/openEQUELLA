<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div id="sortandfilter" class="skinnysearch">
	<div class="buttons">
		<#if !m.saveAndShareDisabled>
			<@button section=s.share />
			<@button section=s.save />
		</#if>
		<@button section=s.sort />
		<@button section=s.filter />
	</div>
	<#if m.childSections??>
		<div class="sortandfilter">
			<@renderList m.childSections />
		</div>
	</#if>
</@div>