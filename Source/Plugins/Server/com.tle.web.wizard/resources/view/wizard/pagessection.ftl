<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area">
	<h2>${m.pageTitle}</h2>
	<#if m.errors?has_content>
	<div class="error-receipt" tabIndex="0" >	
		<p>${b.key("error.receipt")}</p>
		<ul> 
			<#list m.errors as error> <li> ${error} </li> </#list> 
		</ul>
	</div>	 
	</#if>
	<div id="wizard-controls" class="wizard-parentcontrol wizard-controls indent0">
		<#list m.pageResults['wizard-controls'] as section><#t/>
			<#if section.result??> 
				<@render section=section.result/><#t/>
			</#if>
		</#list><#t/>
	</div>
</div>
