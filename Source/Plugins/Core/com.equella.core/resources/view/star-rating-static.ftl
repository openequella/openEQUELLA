<#include "/com.tle.web.freemarker@/macro/sections/util.ftl"/>

<#macro starrating rating classes="">
	<@css path="star-rating-static.css" plugin="com.tle.web.sections.standard" />
	<div class="star-rating-static ${classes}">
		<div class="star-rating-static-${rating}"></div>
	</div>	
</#macro>
