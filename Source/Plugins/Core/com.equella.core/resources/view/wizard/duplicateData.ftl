<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="dupedata.css" />

<div class="area">
	<h2>${b.key('duplicatedatasection.pagename')}</h2>
  <#if m.mustChangeAny>
    <p style="margin-bottom: 0">${b.key("duplicatedatasection.mustchange")}</p>
  </#if>

  <div class="duplicate_editbox_section">
  <#if m.textfieldDuplicateData?size gt 0>
    <#list m.textfieldDuplicateData as duplicateData>
      <#if duplicateData.visible>
        <h4>${b.key("duplicatedatasection.editboxtext",[duplicateData.wizardControlTitle])}</h4>
        <div class="input">
          <label>
            <#if !duplicateData.canAccept>
              <span class="mandatory">${b.key("duplicatedatasection.mustchangesymbol")}</span>
            </#if>
            <span>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</span>
          </label>
          <ul class="blue">
            <#list duplicateData.items as item>
              <li><@render item.link/></li>
            </#list>
          </ul>
        </div>
      </#if>
    </#list>
  </#if>
  </div>

  <div class="duplicate_attachment_section">
  <#if m.attachmentDuplicateData?size gt 0>
    <h4>${b.key("duplicatedatasection.attachmenttext")}</h4>
    <#list m.attachmentDuplicateData as duplicateData>
        <label>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</label>
        <ul class="blue">
          <#list duplicateData.items as item>
            <li><@render item.link/></li>
          </#list>
        </ul>
    </#list>
  </#if>
  </div>

</div>
