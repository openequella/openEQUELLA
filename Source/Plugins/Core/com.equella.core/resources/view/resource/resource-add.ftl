<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@render s.resultsCallback/>

<div class="universaliframe">
	<iframe id="selectresource" src="${m.integrationUrl?html}" frameBorder="0"></iframe>
</div>