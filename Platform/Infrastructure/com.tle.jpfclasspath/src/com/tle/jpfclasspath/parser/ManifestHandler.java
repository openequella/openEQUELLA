/**
 * *************************************************************************** Java Plug-in
 * Framework (JPF) Copyright (C) 2004-2007 Dmitry Olshansky This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ***************************************************************************
 */
package com.tle.jpfclasspath.parser;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/** @version $Id$ */
final class ManifestHandler extends BaseHandler {
  private ModelPluginManifest manifest = null;
  private ModelDocumentation documentation = null;
  private ModelPrerequisite prerequisite;
  private ModelLibrary library;
  private ModelExtensionPoint extensionPoint;
  private ModelExtension extension;
  private StringBuilder docText = null;
  private SimpleStack<ModelAttribute> attributeStack = null;
  private ModelAttribute attribute;
  private SimpleStack<ModelParameterDef> paramDefStack = null;
  private ModelParameterDef paramDef;
  private SimpleStack<ModelParameter> paramStack = null;
  private ModelParameter param;
  private StringBuilder paramValue = null;

  ManifestHandler(final EntityResolver anEntityResolver) {
    super(anEntityResolver);
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String,
   *     java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(
      final String uri, final String localName, final String qName, final Attributes attributes)
      throws SAXException {
    if (log.isDebugEnabled()) {
      log.debug(
          "startElement - ["
              + uri
              + "]/[" //$NON-NLS-1$ //$NON-NLS-2$
              + localName
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String name = qName;
    if ("plugin".equals(name)) { // $NON-NLS-1$
      if (manifest != null) {
        throw new SAXException(
            "unexpected ["
                + name //$NON-NLS-1$
                + "] element (manifest already defined)"); //$NON-NLS-1$
      }
      manifest = new ModelPluginDescriptor();
      manifest.setId(attributes.getValue("id")); // $NON-NLS-1$
      manifest.setVersion(attributes.getValue("version")); // $NON-NLS-1$
      manifest.setVendor(attributes.getValue("vendor")); // $NON-NLS-1$
      manifest.setDocsPath(attributes.getValue("docs-path")); // $NON-NLS-1$
      ((ModelPluginDescriptor) manifest).setClassName(attributes.getValue("class")); // $NON-NLS-1$
    } else if ("plugin-fragment".equals(name)) { // $NON-NLS-1$
      if (manifest != null) {
        throw new SAXException(
            "unexpected ["
                + name //$NON-NLS-1$
                + "] element (manifest already defined)"); //$NON-NLS-1$
      }
      manifest = new ModelPluginFragment();
      manifest.setId(attributes.getValue("id")); // $NON-NLS-1$
      manifest.setVersion(attributes.getValue("version")); // $NON-NLS-1$
      manifest.setVendor(attributes.getValue("vendor")); // $NON-NLS-1$
      manifest.setDocsPath(attributes.getValue("docs-path")); // $NON-NLS-1$
      ((ModelPluginFragment) manifest).setPluginId(attributes.getValue("plugin-id")); // $NON-NLS-1$
      if (attributes.getValue("plugin-version") != null) { // $NON-NLS-1$
        ((ModelPluginFragment) manifest)
            .setPluginVersion(attributes.getValue("plugin-version")); // $NON-NLS-1$
      }
      if (attributes.getValue("match") != null) { // $NON-NLS-1$
        ((ModelPluginFragment) manifest)
            .setMatchingRule(MatchingRule.fromCode(attributes.getValue("match"))); // $NON-NLS-1$
      } else {
        ((ModelPluginFragment) manifest).setMatchingRule(MatchingRule.COMPATIBLE);
      }
    } else if ("doc".equals(name)) { // $NON-NLS-1$
      documentation = new ModelDocumentation();
      documentation.setCaption(attributes.getValue("caption")); // $NON-NLS-1$
    } else if ("doc-ref".equals(name)) { // $NON-NLS-1$
      if (documentation == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[doc-ref] element found " //$NON-NLS-1$
                  + "outside of [doc] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn("[doc-ref] element found outside of [doc] element"); // $NON-NLS-1$
        return;
      }
      ModelDocumentationReference docRef = new ModelDocumentationReference();
      docRef.setPath(attributes.getValue("path")); // $NON-NLS-1$
      docRef.setCaption(attributes.getValue("caption")); // $NON-NLS-1$
      documentation.getReferences().add(docRef);
    } else if ("doc-text".equals(name)) { // $NON-NLS-1$
      if (documentation == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[doc-text] element found " //$NON-NLS-1$
                  + "outside of [doc] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn("[doc-text] element found outside of [doc] element"); // $NON-NLS-1$
        return;
      }
      docText = new StringBuilder();
    } else if ("attributes".equals(name)) { // $NON-NLS-1$
      attributeStack = new SimpleStack<ModelAttribute>();
    } else if ("attribute".equals(name)) { // $NON-NLS-1$
      if (attributeStack == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[attribute] element found " //$NON-NLS-1$
                  + "outside of [attributes] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn(
            "[attribute] element found " //$NON-NLS-1$
                + "outside of [attributes] element"); //$NON-NLS-1$
        return;
      }
      if (attribute != null) {
        attributeStack.push(attribute);
      }
      attribute = new ModelAttribute();
      attribute.setId(attributes.getValue("id")); // $NON-NLS-1$
      attribute.setValue(attributes.getValue("value")); // $NON-NLS-1$
    } else if ("requires".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("import".equals(name)) { // $NON-NLS-1$
      prerequisite = new ModelPrerequisite();
      if (attributes.getValue("id") != null) { // $NON-NLS-1$
        prerequisite.setId(attributes.getValue("id")); // $NON-NLS-1$
      }
      prerequisite.setPluginId(attributes.getValue("plugin-id")); // $NON-NLS-1$
      if (attributes.getValue("plugin-version") != null) { // $NON-NLS-1$
        prerequisite.setPluginVersion(attributes.getValue("plugin-version")); // $NON-NLS-1$
      }
      if (attributes.getValue("match") != null) { // $NON-NLS-1$
        prerequisite.setMatchingRule(
            MatchingRule.fromCode(attributes.getValue("match"))); // $NON-NLS-1$
      } else {
        prerequisite.setMatchingRule(MatchingRule.COMPATIBLE);
      }
      prerequisite.setExported(attributes.getValue("exported")); // $NON-NLS-1$
      prerequisite.setOptional(attributes.getValue("optional")); // $NON-NLS-1$
      prerequisite.setReverseLookup(attributes.getValue("reverse-lookup")); // $NON-NLS-1$
    } else if ("runtime".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("library".equals(name)) { // $NON-NLS-1$
      library = new ModelLibrary();
      library.setId(attributes.getValue("id")); // $NON-NLS-1$
      library.setPath(attributes.getValue("path")); // $NON-NLS-1$
      library.setCodeLibrary(attributes.getValue("type")); // $NON-NLS-1$
      if (attributes.getValue("version") != null) { // $NON-NLS-1$
        library.setVersion(attributes.getValue("version")); // $NON-NLS-1$
      }
    } else if ("export".equals(name)) { // $NON-NLS-1$
      if (library == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[export] element found " //$NON-NLS-1$
                  + "outside of [library] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn("[export] element found outside of [library] element"); // $NON-NLS-1$
        return;
      }
      library.getExports().add(attributes.getValue("prefix")); // $NON-NLS-1$
    } else if ("extension-point".equals(name)) { // $NON-NLS-1$
      extensionPoint = new ModelExtensionPoint();
      extensionPoint.setId(attributes.getValue("id")); // $NON-NLS-1$
      extensionPoint.setParentPluginId(attributes.getValue("parent-plugin-id")); // $NON-NLS-1$
      extensionPoint.setParentPointId(attributes.getValue("parent-point-id")); // $NON-NLS-1$
      if (attributes.getValue("extension-multiplicity") != null) { // $NON-NLS-1$
        extensionPoint.setExtensionMultiplicity(
            ExtensionMultiplicity.fromCode(
                attributes.getValue("extension-multiplicity"))); // $NON-NLS-1$
      } else {
        extensionPoint.setExtensionMultiplicity(ExtensionMultiplicity.ANY);
      }
      paramDefStack = new SimpleStack<ModelParameterDef>();
    } else if ("parameter-def".equals(name)) { // $NON-NLS-1$
      if (extensionPoint == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[parameter-def] element found " //$NON-NLS-1$
                  + "outside of [extension-point] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn(
            "[parameter-def] element found " //$NON-NLS-1$
                + "outside of [extension-point] element"); //$NON-NLS-1$
        return;
      }
      if (paramDef != null) {
        paramDefStack.push(paramDef);
      }
      paramDef = new ModelParameterDef();
      paramDef.setId(attributes.getValue("id")); // $NON-NLS-1$
      if (attributes.getValue("multiplicity") != null) { // $NON-NLS-1$
        paramDef.setMultiplicity(
            ParameterMultiplicity.fromCode(attributes.getValue("multiplicity"))); // $NON-NLS-1$
      } else {
        paramDef.setMultiplicity(ParameterMultiplicity.ONE);
      }
      if (attributes.getValue("type") != null) { // $NON-NLS-1$
        paramDef.setType(ParameterType.fromCode(attributes.getValue("type"))); // $NON-NLS-1$
      } else {
        paramDef.setType(ParameterType.STRING);
      }
      paramDef.setCustomData(attributes.getValue("custom-data")); // $NON-NLS-1$
      paramDef.setDefaultValue(attributes.getValue("default-value")); // $NON-NLS-1$
    } else if ("extension".equals(name)) { // $NON-NLS-1$
      extension = new ModelExtension();
      extension.setId(attributes.getValue("id")); // $NON-NLS-1$
      extension.setPluginId(attributes.getValue("plugin-id")); // $NON-NLS-1$
      extension.setPointId(attributes.getValue("point-id")); // $NON-NLS-1$
      paramStack = new SimpleStack<ModelParameter>();
    } else if ("parameter".equals(name)) { // $NON-NLS-1$
      if (extension == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[parameter] element found " //$NON-NLS-1$
                  + "outside of [extension] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn(
            "[parameter] element found " //$NON-NLS-1$
                + "outside of [extension] element"); //$NON-NLS-1$
        return;
      }
      if (param != null) {
        paramStack.push(param);
      }
      param = new ModelParameter();
      param.setId(attributes.getValue("id")); // $NON-NLS-1$
      param.setValue(attributes.getValue("value")); // $NON-NLS-1$
    } else if ("value".equals(name)) { // $NON-NLS-1$
      if (param == null) {
        if (entityResolver != null) {
          throw new SAXException(
              "[value] element found " //$NON-NLS-1$
                  + "outside of [parameter] element"); //$NON-NLS-1$
        }
        // ignore this element
        log.warn(
            "[value] element found " //$NON-NLS-1$
                + "outside of [parameter] element"); //$NON-NLS-1$
        return;
      }
      paramValue = new StringBuilder();
    } else {
      if (entityResolver != null) {
        throw new SAXException(
            "unexpected manifest element - [" //$NON-NLS-1$
                + uri
                + "]/["
                + localName
                + "]/["
                + qName
                + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
      // ignore this element
      log.warn(
          "unexpected manifest element - ["
              + uri
              + "]/[" //$NON-NLS-1$ //$NON-NLS-2$
              + localName
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String,
   *     java.lang.String)
   */
  @Override
  public void endElement(final String uri, final String localName, final String qName) {
    if (log.isDebugEnabled()) {
      log.debug(
          "endElement - ["
              + uri
              + "]/["
              + localName //$NON-NLS-1$ //$NON-NLS-2$
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String name = qName;
    if ("plugin".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("plugin-fragment".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("doc".equals(name)) { // $NON-NLS-1$
      if (param != null) {
        param.setDocumentation(documentation);
      } else if (extension != null) {
        extension.setDocumentation(documentation);
      } else if (paramDef != null) {
        paramDef.setDocumentation(documentation);
      } else if (extensionPoint != null) {
        extensionPoint.setDocumentation(documentation);
      } else if (library != null) {
        library.setDocumentation(documentation);
      } else if (prerequisite != null) {
        prerequisite.setDocumentation(documentation);
      } else if (attribute != null) {
        attribute.setDocumentation(documentation);
      } else {
        manifest.setDocumentation(documentation);
      }
      documentation = null;
    } else if ("doc-ref".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("doc-text".equals(name)) { // $NON-NLS-1$
      documentation.setText(docText.toString());
      docText = null;
    } else if ("attributes".equals(name)) { // $NON-NLS-1$
      attributeStack = null;
    } else if ("attribute".equals(name)) { // $NON-NLS-1$
      if (attributeStack.size() == 0) {
        manifest.getAttributes().add(attribute);
        attribute = null;
      } else {
        ModelAttribute temp = attribute;
        attribute = attributeStack.pop();
        attribute.getAttributes().add(temp);
        temp = null;
      }
    } else if ("requires".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("import".equals(name)) { // $NON-NLS-1$
      manifest.getPrerequisites().add(prerequisite);
      prerequisite = null;
    } else if ("runtime".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("library".equals(name)) { // $NON-NLS-1$
      manifest.getLibraries().add(library);
      library = null;
    } else if ("export".equals(name)) { // $NON-NLS-1$
      // no-op
    } else if ("extension-point".equals(name)) { // $NON-NLS-1$
      manifest.getExtensionPoints().add(extensionPoint);
      extensionPoint = null;
      paramDefStack = null;
    } else if ("parameter-def".equals(name)) { // $NON-NLS-1$
      if (paramDefStack.size() == 0) {
        extensionPoint.getParamDefs().add(paramDef);
        paramDef = null;
      } else {
        ModelParameterDef temp = paramDef;
        paramDef = paramDefStack.pop();
        paramDef.getParamDefs().add(temp);
        temp = null;
      }
    } else if ("extension".equals(name)) { // $NON-NLS-1$
      manifest.getExtensions().add(extension);
      extension = null;
      paramStack = null;
    } else if ("parameter".equals(name)) { // $NON-NLS-1$
      if (paramStack.size() == 0) {
        extension.getParams().add(param);
        param = null;
      } else {
        ModelParameter temp = param;
        param = paramStack.pop();
        param.getParams().add(temp);
        temp = null;
      }
    } else if ("value".equals(name)) { // $NON-NLS-1$
      param.setValue(paramValue.toString());
      paramValue = null;
    } else {
      // ignore any other element
      log.warn(
          "ignoring manifest element - ["
              + uri
              + "]/[" //$NON-NLS-1$ //$NON-NLS-2$
              + localName
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /** @see org.xml.sax.ContentHandler#characters(char[], int, int) */
  @Override
  public void characters(final char[] ch, final int start, final int length) throws SAXException {
    if (docText != null) {
      docText.append(ch, start, length);
    } else if (paramValue != null) {
      paramValue.append(ch, start, length);
    } else {
      if (entityResolver != null) {
        throw new SAXException("unexpected character data"); // $NON-NLS-1$
      }
      // ignore these characters
      log.warn(
          "ignoring character data - [" //$NON-NLS-1$
              + new String(ch, start, length)
              + "]"); //$NON-NLS-1$
    }
  }

  ModelPluginManifest getResult() {
    return manifest;
  }
}
