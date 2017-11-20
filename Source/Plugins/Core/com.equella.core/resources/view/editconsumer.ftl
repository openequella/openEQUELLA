<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>

<@css "editconsumer.css" />

<@setting label=b.key("editor.key") mandatory=true error=m.errors["key"]>
	<@textfield section=s.consumerKeyField maxlength=255 />
</@setting>
<@setting label=b.key("editor.secret") mandatory=true error=m.errors["secret"]>
	<@textfield section=s.consumerSecretField maxlength=255 />
</@setting>
<@setting label=b.key("editor.prefix") help=b.key("editor.username.help")>
	<@textfield section=s.prefixField maxlength=50 />
</@setting>
<@setting label=b.key("editor.postfix") help=b.key("editor.username.help")>
	<@textfield section=s.postfixField maxlength=50 />
</@setting>
<@a.div id="allowed">
	<@setting label=b.key("editor.allowed.label") help=b.key("editor.allowed.help")>
		${m.prettyExpression}
		<@button section=s.allowedSelector.opener showAs="select_user">${b.key("editor.button.change")}</@button>
	</@setting>
</@a.div>
<@setting label=b.key("editor.unknown.label") help=b.key("editor.unknown.help") section=s.unknownUserList />
<@a.div id="unknownusergroup">
	<#if m.selectGroups>
		<@setting label=b.key("editor.unknown.groups.label") section=s.unknownUserGroupsTable />
	</#if>
</@a.div>
<hr>
<h2>${b.key("editor.role.title")}</h2>
<@a.div id="instructorrole">
	<@setting label=b.key("editor.role.instructor.label") help=b.key("editor.role.instructor.help") section=s.instructorRolesTable />
</@a.div>
<@a.div id="customrole">
	<@setting label=b.key("editor.role.custom.label") error=m.errors["nocustomrole"] help=b.key("editor.role.custom.help")>
		<@render section=s.customRolesTable />	
		<@autocomplete section=s.customRoleField class="custom-role" placeholder=b.key("editor.role.custom.placeholder")  /> 
		<@render section=s.customRoleDialog.opener  class="add">${b.key("editor.table.roles.add")}</@render>
	</@setting>
</@a.div>
<@a.div id="otherrole">
	<@setting label=b.key("editor.role.unknown.label") help=b.key("editor.role.unknown.help") section=s.otherRolesTable />
</@a.div>
