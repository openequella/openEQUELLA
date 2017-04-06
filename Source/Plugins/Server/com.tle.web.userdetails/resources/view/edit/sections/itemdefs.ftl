<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="userdetails.css" hasRtl=true/>


<div class="edit">
	<h3>${b.key('common.notifications')}</h3>
	<p>${b.key('notifications.email.help')}</p>
	<div class="input checkbox">
		<@render id="enableEmails" section=s.enableEmails />
		<label for="enableEmails">${b.key('notifications.email.enabled')}</label>
	</div>
	<label for="${s.itemDefs}">
		<p>${b.key('common.selectwatch')}</p>
	</label>
	<div class="itemdefs_container">
		<@render section=s.itemDefs class="col_checklist"/>
	</div> 
</div>
