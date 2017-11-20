<#include "emailhelper.ftl">
<@stdHeader/>
<#list m.notifications as t>
<@table>
<@row b.key('email.itemname')>${t.itemName}</@row>
<@row b.key('email.owner')>${t.owner}</@row>
<@row b.key('email.taskname')>${t.taskName}</@row>
<#if t.dueDate??>
<@row b.key('email.taskdue')>${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if></@row>
</#if>
<@taskLink t/>
</@table>
</#list>
<@stdFooter/>