<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "modcomments.css"/>

<#list m as comment>
    <div class="modcomment ${comment.extraClass}">
        <div class="modcomment-avatar"></div>
        <div class="modcomment-content">
            <span class="modcomment-username"><@render comment.user/></span>
            <@render section=comment.dateRenderer class="modcomment-date"/>
            <#if comment.taskname??>
                <div class="modcomment-task">${b.key('comments.taskname')}: ${comment.taskName}</div>
            </#if>
            <#if comment.message??>
                <div class="modcomment-content">
                    ${comment.message?html?replace("\n", "<br>")}
                </div>
            </#if>
            <#if comment.attachments??>
                <ul>
                    <#list comment.attachments as filelink>
                        <li><@render filelink/></li>
                    </#list>
                </ul>
            </#if>
        </div>
    </div>
</#list>
