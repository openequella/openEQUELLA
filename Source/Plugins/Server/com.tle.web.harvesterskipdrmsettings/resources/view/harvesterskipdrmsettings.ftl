<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<div class="area">
	<h2>${b.key('harvesterskipdrmsettings.title')}</h2>
	<div class="input checkbox">
		<@setting label=b.key('harvesterskipdrmsettings.checklabel') section=s.allowSkip />
	</div>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>
