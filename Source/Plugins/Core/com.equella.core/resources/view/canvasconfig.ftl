<?xml version="1.0" encoding="UTF-8"?>
<cartridge_basiclti_link xmlns="http://www.imsglobal.org/xsd/imslticc_v1p0"
    xmlns:blti = "http://www.imsglobal.org/xsd/imsbasiclti_v1p0"
    xmlns:lticm ="http://www.imsglobal.org/xsd/imslticm_v1p0"
    xmlns:lticp ="http://www.imsglobal.org/xsd/imslticp_v1p0"
    xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation = "http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd
    http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0.xsd
    http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd
    http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd">
    <blti:title>${b.key('tool.title')}</blti:title>
    <blti:description>${b.key('tool.description')}</blti:description>
    <blti:icon>${m.instUrl}images/equella.gif</blti:icon>
    <blti:launch_url>${m.instUrl}lti/toolprovider</blti:launch_url>
    <blti:extensions platform="canvas.instructure.com">
      <lticm:property name="tool_id">equella_canvas</lticm:property>
      <lticm:property name="privacy_level">public</lticm:property>
      <lticm:property name="domain">${m.domain}</lticm:property>
      <lticm:property name="icon_url">${m.instUrl}images/equella.gif</lticm:property>
      <lticm:property name="selection_height">768</lticm:property>
      <lticm:property name="selection_width">1024</lticm:property>
      <blti:custom>
        <lticm:property name="course_id">$Canvas.course.sisSourceId</lticm:property>
        <lticm:property name="course_code">$com.instructure.contextLabel</lticm:property>
      </blti:custom>
      
      <lticm:options name="resource_selection">
      	<lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
        <lticm:property name="url">${m.instUrl}canvassignon.do?action=selectOrAdd&amp;cancelDisabled=true</lticm:property>
        <lticm:property name="icon_url">${m.instUrl}images/equella.gif</lticm:property>
        <lticm:property name="text">${b.key('tool.selectionsession.linktext')}</lticm:property>
        <lticm:property name="selection_width">${m.width}</lticm:property>
        <lticm:property name="selection_height">${m.height}</lticm:property>
        <lticm:property name="enabled">true</lticm:property>
      </lticm:options>
      
      <lticm:options name="course_navigation">
        <lticm:property name="url">${m.instUrl}canvassignon.do?action=structured&amp;selection_directive=select_link&amp;cancelDisabled=true</lticm:property>
        <lticm:property name="text">${b.key('tool.menu.selectionlinktext')}</lticm:property>
        <lticm:property name="visibility">admins</lticm:property>
        <lticm:property name="default">enabled</lticm:property>
        <lticm:property name="enabled">true</lticm:property>
      </lticm:options>
      
      <lticm:options name="editor_button"> 
      	<lticm:property name="canvas_icon_class">icon-lti</lticm:property>
        <lticm:property name="icon_url">${m.instUrl}images/equella.gif</lticm:property>
        <lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
        <lticm:property name="text">${b.key('tool.editor.buttontext')}</lticm:property>
        <lticm:property name="selection_width">${m.width}</lticm:property>
        <lticm:property name="selection_height">${m.height}</lticm:property>
        <lticm:property name="url">${m.instUrl}canvassignon.do?action=selectOrAdd&amp;cancelDisabled=true</lticm:property>
      </lticm:options>
    </blti:extensions>
    
    <cartridge_bundle identifierref="BLTI001_Bundle"/>
    <cartridge_icon identifierref="BLTI001_Icon"/>
</cartridge_basiclti_link>  