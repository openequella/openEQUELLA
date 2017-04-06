<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>

<@css "autocompleteeditbox.css"/>

<@div id="${id}autocompleteControl" class="autocompleteControl">
	<#if m.canSelect>
		<@autocomplete section=s.editbox autoSubmitButton=s.selectTerm />
		<@button section=s.selectTerm size="medium" />
	</#if>

	<#if m.errorMessage??>
		<p class="error">${m.errorMessage}</p>
	</#if>
	
	<@render s.termsTable />
</@div>