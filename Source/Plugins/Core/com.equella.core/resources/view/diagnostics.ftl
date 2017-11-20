<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/list.ftl" />

<div class="area">
	<h2>${b.key('diagnostics.title')}</h2>
	<@a.div id="usergroups">
		<h3>${b.key("viewusers")}</h3>
		<p>${b.key("viewusers.help")}</p>
	
		<div class="button-user">
			<@button section=s.userGroupsDialog.opener>
				<#if m.userSelected>
					${b.key("change.user")}
				<#else>
					${b.key("select.user")}
				</#if>
			</@button>
		</div>
		<#if s.groupsTable??>
			<div style="max-height: 200px; overflow: auto">
				<@render s.groupsTable />
			</div>
		</#if>
	</@a.div>
	<@a.div id="groupmembers">
		<h3>${b.key("viewgroups")}</h3>
		<p>${b.key("viewgroups.help")}</p>

		<div class="button-group">
			<@button section=s.selectGroupDialog.opener>
				<#if m.groupSelected>
					${b.key("change.group")}
				<#else>
					${b.key("select.group")}
				</#if>
			</@button>
		</div>

		<#if s.usersTable??>
			<div style="max-height: 200px; overflow: auto">
				<@render s.usersTable />
			</div>
		</#if>
	</@a.div>
</div>
