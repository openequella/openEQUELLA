<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<h1>${b.key('databases.duplicate.title')}</h1>

<#assign schemaLabel>${m.schemaLabel}</#assign>
<p>${b.key('databases.duplicate.info',[schemaLabel])}</p>

