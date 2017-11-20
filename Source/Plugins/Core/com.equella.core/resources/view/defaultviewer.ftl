<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#import "/com.tle.web.sections.standard@/dialog.ftl" as d>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@a.div id="viewers">
	<h3>${b.key("tab.viewers")}</h3>

	<#list m.dialogs as dialog>
		<@d.dialog dialog/>
	</#list>
	
	<@render s.defaultViewersTable />
</@a.div>

