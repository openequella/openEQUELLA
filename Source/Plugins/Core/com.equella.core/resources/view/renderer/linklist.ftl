<#import "/com.tle.web.sections.standard@/list.ftl" as l/>

<#macro linklist section id="">
	<@l.buttonlist id=id section=section hideDisabledOptions=true; opt, button, selected, last>
		<#if !selected>
			<@render button/>
		<#else>
			<strong>${button.label}</strong>
		</#if>
		<#if !last><span class="separator"> | </span></#if>
	</@l.buttonlist>
</#macro>