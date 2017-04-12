<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@div id="main">
	<@settingContainer mandatory=true wide=true >
		<@setting
			 label=b.key('login.ipaddressesdialog')
			 help=b.key('login.ipaddressesdialog.help')
			 mandatory=true labelFor=s.ipAddressText >
			 
			<@textfield section=s.ipAddressText maxlength=64 class="focus"/>
		
		</@setting>
	</@>
</@div>