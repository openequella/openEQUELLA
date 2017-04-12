<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "language.css" />

<@div id="main">
	<@settingContainer mandatory=false>
		<@setting label=b.key('addlanguage.dialog.language.label') labelFor=s.languageList>
			<div class="input select">
				<@render section=s.languageList />
			</div>
		</@>
		<@setting label=b.key('addlanguage.dialog.country.label') labelFor=s.countryList>
			<div class="input select langlist">
				<@render section=s.countryList />
			</div>
		</@>
	</@>
</@>