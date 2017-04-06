<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@css path="flickr.css" hasRtl=true />

<@div class="filter" id="flickr-institutions">
	<#if s.flickrInstitutionSelector.isDisplayed(_info)>
		<h3>${b.key('filter.flickrInstitution')}</h3>
		<div class="input select">
			<@render section=s.flickrInstitutionSelector />
		</div>
	</#if>
</@div>
<hr/>
