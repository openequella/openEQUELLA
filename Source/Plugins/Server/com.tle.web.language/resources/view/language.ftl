<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "language.css" />
<@css path="component/zebratable.css" plugin="com.tle.web.sections.equella" hasRtl=true/>

<div class="area">
	<h2>${b.key('language.separator.langpack')}</h2>
	<label>${b.key('langpack.subheading')}</label>
	<@settingContainer mandatory=false>
		<@div id="languagepacklist">
			<div class="input">
				<@render section=s.languagePacksTbl />
			</div>
		</@>
		<label>${b.key('language.import.prompt')}</label>
		<div>
			<@file section=s.fileUploader class="uploadfield" />
		</div>
		<div class="importlink">
			<@render section=s.importLocaleLink/>
		</div>
	</@>

	<br>

	<h2>${b.key('language.separator.contribution')}</h2>
	<label>${b.key('contriblang.subheading')}</label>

	<@settingContainer mandatory=false>
		<@div id="contriblanglist">
			<div class="input">
				<@render section=s.contributionLanguageTbl />
			</div>
			<@render section=s.addContribLangLink />
		</@>
	</@>
</div>
