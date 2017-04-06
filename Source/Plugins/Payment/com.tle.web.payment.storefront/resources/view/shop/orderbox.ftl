<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "shop/orderbox.css" />

<@render section=s.box class="orderbox">
	<table class="ordertable">	
	<#assign rownumber=1>
	<#list m.orders as order>
		<#if rownumber%2 == 0>
			<tr class="even">
		<#else>
			<tr class="odd">
		</#if>
		<#assign rownumber=rownumber+1>
		<td class="firstcolumn"><@render order.link>${order.date()?date?string.medium} <#if order.status??>${order.status}<#else>${order.userLabel}</#if></@render></td> 
		<td class="secondcolumn">${order.total}</td>
		</tr>
	</#list>
	</table>
</@render>