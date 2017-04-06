<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<h3>${b.key("tab.details")}</h3>
<#if m.errorKey??>
	<@setting label="" error=b.key("error." + m.errorKey) />
</#if>

<@setting label=b.key('label.description') section=s.description />
<@setting label=b.key('label.type') section=s.type mandatory=true />
<@setting label=b.key('label.extensions') labelFor=s.newExtension >
	<@render s.newExtension />
	<div class="extension-buttons">
		<@button s.addExtensionButton><@bundlekey "button.addextension" class="button"/></@button>
		<@button s.removeExtensionButton><@bundlekey "button.removeextension" class="button"/></@button>
	</div>
	<@dropdown section=s.extensions size=6 class="extensionslist" />
</@setting>