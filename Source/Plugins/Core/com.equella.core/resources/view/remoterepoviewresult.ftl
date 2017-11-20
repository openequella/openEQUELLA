<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<#assign result=m.result />

<div class="area">
	<h2><@render m.title /></h2>

	<#if m.resourceUrl??>
		<#if m.hasUrl>
			<a href="${m.resourceUrl?html}"/><@render m.resourceUrl/></a>
		<#else>
			<@render m.resourceUrl/>
		</#if>
	</#if>
	<#if m.content?size gt 0>
	<div class="details">
		<#list m.content as c>	
			<@render c/>
		</#list>
	</div>
	</#if>
	
	<div class="button-strip">
		<@button section=s.importButton showAs="save"><@bundlekey "view.button.import"/></@button>
	</div>
</div>