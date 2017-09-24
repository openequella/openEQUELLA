<#include "emailhelper.ftl">
<@stdHeader/>
<#list m.notifications as t>
<@table>
<@row label=b.key('email.itemname')>${t.itemName}</@row>
<@row label=b.key('email.owner')>${t.owner}</@row>

<#if t.hasCauseInfo>
<#if t.causeLabel??>
<@row label=t.causeLabel>${t.causeTask}</@row>
</#if>
<#list t.messages as msg>
<@row msg.label>${b.key('email.msgformat',[msg.message, msg.by])}</@row>
<#if msg.hasFiles>
<@row msg.fileLabel><#list msg.files as f><a href="${f.value}">${f.name}</a><br></#list></@row>
</#if>
</#list>

</#if>
<@row b.key('email.currenttask')>${t.taskName}</@row>
<#if t.dueDate??>
<@row b.key('email.taskdue')>${t.dueDate()?date?string.medium} <#if t.autoAction??>${t.autoAction}</#if></@row>
</#if>
<@taskLink t/>

</@table>
</#list>
<@stdFooter/>