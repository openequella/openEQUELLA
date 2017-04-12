<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<div class="area">
<h2>${b.key("store.view")}</h2>
<h3>${b.key("store.view.details")}</h3>

	<@render s.storeDetails />




<h3>${b.key('store.view.contact')}</h3>

	<@render s.contactDetails />

<div class="button-strip"><@button section=s.backButton showAs="cancel"/></div>

	
</div>