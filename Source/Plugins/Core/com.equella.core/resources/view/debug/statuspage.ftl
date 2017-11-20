<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#assign u = m.userState.userBean>
<h2>Logged In User</h2> 

<div class="userid">ID: ${u.uniqueID?html}</div>
<div class="username">Username: ${u.username?html}</div>

<h2>Groups</h2>
<#list m.groups as group>
<div class="grouprow"><span class="name">${group.name?html}</span> (<span class="id">${group.uniqueID?html}</span>)</div>
</#list>
<#if !m.groups?has_content>
<div>None</div>
</#if>

<h2>Roles</h2>
<#list m.roles as role>
<div class="rolerow"><span class="name">${role.name?html}</span> (<span class="id">${role.uniqueID?html}</span>)</div>
</#list>
<#if !m.roles?has_content>
<div>None</div>
</#if>
