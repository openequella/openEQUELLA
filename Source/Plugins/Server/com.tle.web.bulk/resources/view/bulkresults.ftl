<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css "bulkresults.css"/>
<div id="bulkresults_dialog">
	<div class="focus" tabIndex="0">${m.operationTitle}</div>
	<div id="bulkresults_container">
		<@render section=s.bulkResultsTable class="bulkresults_resultlist" />
	</div>	
	<div id="bulkresults_templates" style="display: none;">
		<table>
			<tr class="failedmsg" tabIndex="0">
				<td class="itemname"></td>
				<td class="status">${b.key('opresults.failed')}<span class="reason"></span></td>
			</tr>
			
			<tr class="succeedmsg" tabindex="0">
				<td class="itemname"></td>
				<td class="status">${b.key('opresults.succeeded')}</td>
			</tr>
		</table>
	</div>
</div>