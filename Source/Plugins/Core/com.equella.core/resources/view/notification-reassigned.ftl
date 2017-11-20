<#include "emailhelper.ftl">
<@stdHeader/>
<#list m.notifications as t>
<@table>
<@row b.key('email.itemname')>${t.itemName}</@row>
<@row b.key('email.owner')>${t.owner}</@row>
<@row b.key('email.taskname')>${t.taskName}</@row>
<@taskLink t/>
</@table>
</#list>
<@stdFooter/>