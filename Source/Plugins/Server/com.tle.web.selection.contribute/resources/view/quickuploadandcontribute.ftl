<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">

<@css "quickuploadandcontribute.css"/>

<@render s.box>
	<#if m.contribute>
		<h4>${b.key("contributenew")}</h4>
		<div class="contribute-div">
			<@button section=s.contributeButton showAs="goto" />
		</div>
	</#if>
	<#if m.contribute && m.quickUpload><hr></#if>
	<#if m.errorKey??><span class="mandatory"><@bundlekey m.errorKey /></span></#if>
	<#if m.quickUpload>
		<@render s.qus />
	</#if>
</@render>