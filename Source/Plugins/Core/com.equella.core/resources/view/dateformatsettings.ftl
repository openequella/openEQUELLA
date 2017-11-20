<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css path="dateformatsettings.css" hasRtl=true />

<div class="area">
	<h2>${b.key('dates.settings.page.title')}</h2>
	
	<div class="input checkbox">
		<@checklist section=s.dateFormats list=true />
	</div>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>