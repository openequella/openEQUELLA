<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<div class="area">
	<h2>${b.key('oaiidentifier.title')}</h2>
	<@settingContainer wide=true  mandatory=false>
		<@setting label=b.key('oaiidentifier.scheme') help=b.key('oaiidentifier.schemeHelp') labelFor=s.oaiSchemeText>
			<@textfield section=s.oaiSchemeText />
		</@setting>
		<@setting label=b.key('oaiidentifier.namespaceIdentifier') help=b.key('oaiidentifier.namespaceIdentifierHelp') labelFor=s.namespaceText>
			<@textfield section=s.namespaceText />
		</@setting>
		<@setting label=b.key('oaiidentifier.email') help=b.key('oaiidentifier.emailHelp') labelFor=s.emailText>
			<@textfield section=s.emailText />
		</@setting>
	</@settingContainer>
	<div class="spacer"></div>
	<h2>${b.key('oaidiscovery.title')}</h2>
	<@settingContainer wide=true mandatory=false>
		<@setting label=b.key('oaidiscovery.downloaditemacl') section=s.useDownloadItemAcl />
	</@settingContainer>


	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>