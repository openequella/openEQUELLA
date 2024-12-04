/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.devlib;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

final class DOMHelper {
  private DOMHelper() {
    throw new IllegalAccessError("Do not invoke");
  }

  /** Removes namespace from a given path element. */
  static String stripNamespace(String name) {
    int index = name.indexOf(':');
    if (index >= 0) {
      name = name.substring(index + 1);
    }
    return name;
  }

  /** Removes namespace from a given path element. */
  static String stripAttribute(String name) {
    if (name.startsWith("@")) {
      return name.substring(1);
    } else {
      return name;
    }
  }

  /** Retrieves the text value from a node's child text node. */
  static String getValueForNode(Node node, String defaultValue) {
    String value = null;
    if (node != null) {
      switch (node.getNodeType()) {
        case Node.ELEMENT_NODE:
          Node textNode = node.getFirstChild();
          if (textNode != null) {
            value = textNode.getNodeValue();
          }
          break;
        case Node.ATTRIBUTE_NODE:
          value = node.getNodeValue();
          break;
        default:
          break;
      }
    }

    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  /** Retrieves the text value from a node's child text node. */
  static void setValueForNode(Node node, String value) {
    if (node != null) {
      switch (node.getNodeType()) {
        case Node.ELEMENT_NODE:
          Node child = node.getFirstChild();
          if (child == null) {
            Document doc = node.getOwnerDocument();
            node.appendChild(doc.createTextNode(value));
          } else {
            child.setNodeValue(value);
          }
          break;
        case Node.ATTRIBUTE_NODE:
          Attr attribute = (Attr) node;
          attribute.getOwnerElement().setAttribute(attribute.getName(), value);
          break;
        default:
          break;
      }
    }
  }

  static Node findNext(Node child, String nodeName) {
    for (; child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (nodeName == null
            || nodeName.equals("*")
            || nodeName.equals(DOMHelper.stripNamespace(child.getNodeName()))) {
          return child;
        }
      }
    }
    return null;
  }

  /**
   * @return a vector containing all the delimited String sections
   */
  static List<String> splitPath(String path) {
    List<String> parts = new ArrayList<String>();

    String[] split = path.split("/");
    for (int i = 0; i < split.length; i++) {
      if (split[i].length() > 0) {
        boolean isLastPart = i == split.length - 1;
        if (!isLastPart && split[i].indexOf('@') >= 0) {
          throw new IllegalArgumentException("Attribute must be last component of path");
        }
        parts.add(split[i]);
      }
    }

    return parts;
  }

  static boolean hasChildElement(Node node) {
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        return true;
      }
    }
    return false;
  }

  static boolean removeNode(Node node) {
    if (node != null) {
      switch (node.getNodeType()) {
        case Node.ELEMENT_NODE:
          Node parent = node.getParentNode();
          if (parent != null) {
            parent.removeChild(node);
            return true;
          }
          break;
        case Node.ATTRIBUTE_NODE:
          Attr attr = (Attr) node;
          attr.getOwnerElement().removeAttribute(attr.getName());
          return true;
        default:
          break;
      }
    }
    return false;
  }
}
