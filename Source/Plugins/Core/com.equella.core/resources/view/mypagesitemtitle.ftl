<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">

<div>
	<div class="control ctrlbody">
		<#if m.error><p class="ctrlinvalidmessage"><@bundlekey value="error.enterdescription"/></p></#if>
		
		<h3 class="ctrltitle"><@bundlekey "description.label" /><span class="mandatory">*</span></h3>
		
		<@textfield section=s.titleField />
	</div>
</div>