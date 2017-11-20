<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@render s.resultsCallback/>

<div class="universaliframe">
	<iframe id="scrapiframe" frameBorder="0" src="${m.selectionUrl?html}"></iframe>
</div>
