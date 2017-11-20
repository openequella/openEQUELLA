<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />

<#if m.showPreview >
<div class="control ctrlbody">
	<h3 class="ctrltitle">${b.key('handlers.mypages.preview')}</h3> 
	
	<div class="input checkbox">
		<@render s.previewCheckBox />
	</div>
</div>
</#if>
<#if m.showRestrict >
<div class="control ctrlbody">
	<h3 class="ctrltitle">${b.key('handlers.mypages.restrict')}</h3> 
	
	<div class="input checkbox">
		<@render s.restrictCheckbox />
	</div>
</div>
</#if>
