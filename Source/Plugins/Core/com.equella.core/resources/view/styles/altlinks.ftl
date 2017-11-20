<#import "/com.tle.web.freemarker@/macro/sections/util.ftl" as u>

<@u.css path="altlinks.css" plugin="com.tle.web.sections.equella" hasRtl=true />

<#function altclass index>
	<#if index % 2 == 0>
		<#return "even">
	<#else>
		<#return "odd">
	</#if>
</#function>