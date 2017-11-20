<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />

<h3>${b.key('pick.heading')}</h3>

<@textfield section=s.selectedHandler hidden=true />
<@render s.pickResourceTypeTable />