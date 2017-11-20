<#include "emailhelper.ftl">
<@stdHeader/>

<#list m.notifications as i>
<@table>
<@row b.key('email.itemlink')><@itemLink i/></@row>
<@row b.key('email.rejectedby')>${i.rejectedBy}</@row>
<@row b.key('email.rejecttask')>${i.rejectedTask}</@row>
<@row b.key('email.msg.r')> ${i.rejectedMessage}</@row>
<tr><td><a href="${i.link.href}?is.summaryId=hc">${b.key('email.modhistory')}</a></td></tr>
</@table>
</#list>
<@stdFooter/>
