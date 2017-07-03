<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="userdetails.css" hasRtl=true/>

<div class="edit">
	<h3>${b.key('common.notifications')}</h3>
	<p>${b.key('common.selectwatch')}</p>
	<div class="itemdefs_container">
		<@render section=s.itemDefs class="col_checklist"/>
	</div> 
</div>
