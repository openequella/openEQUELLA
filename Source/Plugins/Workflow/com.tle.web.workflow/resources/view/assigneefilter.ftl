<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>


<@a.div id=s.ajaxDiv class="filter">
	<h3>${b.key("filter.assignee")}</h3>
	<#if m.owner??>
		<#assign buttonText="filter.byowner.changebutton" />
	<#else>
		<#assign buttonText="filter.byowner.selbutton" />
	</#if>

	<p>
		<#if m.owner??><@render m.owner /></#if>
	</p>
	
	<@button section=s.selectOwner.opener showAs="select_user" style="margin-left: 0">${b.key(buttonText)}</@button>
	
	<#if m.owner??>
		<@button section=s.remove showAs="delete" />
	</#if>
	<div class="input checkbox">
		<@render section=s.unassOnlyCheckbox />
	</div>
</@a.div>
<hr>