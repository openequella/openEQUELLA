<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "searchprevnext.css"/>

<@div id="searchprevnext">
<div class="btn-group">
	<@button section=s.prevButton showAs="prev" size="small" />
	<@button section=s.nextButton showAs="next" size="small" />
</div>
</@>
<div class="clear"></div>
