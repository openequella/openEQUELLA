<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>
<#include "/com.tle.web.sections.standard@/button.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="shop/search.css" hasRtl=true />

<div class="area">	
	<div class="topright"> <@render m.icon /></div>
	<div><h2>${m.title}</h2></div>
	<div class="indent">
		<h4>${m.catalogueName} 
			<#if m.showBackLink>
				( <@render section=s.returnToStore class="viewOthers-link"/> )
			</#if>
		</h4>
		<p>${m.catalogueDescription}</p> 	
	</div>
</div>
