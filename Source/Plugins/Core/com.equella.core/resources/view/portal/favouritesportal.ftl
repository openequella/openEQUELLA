<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#include "/com.tle.web.sections.equella@/styles/altlinks.ftl">

<@css "favourites.css"/>

<#if m.favourites?size &gt; 0>
	<div class="alt-links">
		<#list m.favourites as fav >
			<@render section=fav.link class="${altclass(fav_index)} favourites">${fav.label}</@render>
		</#list>
	</div>
	<div class="button_content">
		<div class="button-strip">
			<@button section=s.showAll showAs="goto" />
		</div>
	</div>
<#else>
	<div class="noresults">
		<p>${b.key("portal.noresults")}</p>
	</div>
</#if>

