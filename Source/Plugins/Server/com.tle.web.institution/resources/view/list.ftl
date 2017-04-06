<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "institutions.css" />

<div class="area">
	<#if m.requireMigrationCount gt 0>
		<p class="warning">
			${b.key('institutions.list.migrationsrequired.' + (m.requireMigrationCount == 1)?string('singular', 'plural'), m.requireMigrationCount)}
			${b.key('institutions.list.migrationsrequired.action')}
		</p>
	</#if>
	<#if m.institutions?size == 0>
		<h2>${b.key("institutions.list.noinstitutions")}</h2>
	<#else>
		<h2>${b.key("institutions.list.select")}</h2>
		<ul class="select_institution_list">
			<#list m.institutions as i>
				<li class="badge-row"><@render i.loginLink><span class="badge-shadow"><img src="${i.badgeUrl?html}" class="institution-badge" alt="${i.name}"></span> <div class="nameEllipsis">${i.name?html}</div></@render></li>
			</#list>
		</ul>
		<br class="clear" />	
	</#if>
</div>
<div class="administerserver">
	<a href="institutions.do?method=admin">${b.key('institutions.list.administer')}</a>
</div>
