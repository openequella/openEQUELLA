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

package com.tle.web.api.search

import com.dytech.devlib.PropBagEx
import com.tle.beans.entity.Schema
import com.tle.beans.entity.Schema.SchemaNode
import com.tle.core.services.item.FreetextResult
import com.tle.legacy.LegacyGuice
import org.apache.commons.lang.StringEscapeUtils
import java.io.BufferedOutputStream
import scala.jdk.CollectionConverters._

case class CSVHeader(name: String, xpath: String)

object ExportCSVHelper {
  val DRM_HEADER = "DRM"
  val STANDARD_HEADER_LIST: List[CSVHeader] = List(
    ("Item ID", "item/@id"),
    ("Item status", "item/@itemstatus"),
    ("Collection", "collection"),
    ("Moderating", "item/@moderating"),
    ("Version", "item/@version"),
    ("New Item", "item/newitem"),
    ("Thumbnail", "item/thumbnail"),
    ("Owner", "item/owner"),
    ("Date created", "item/datecreated"),
    ("Date modified", "item/datemodified"),
    ("Date indexed", "item/dateforindex"),
    ("Rating", "item/rating/@average"),
    ("Attachments", "item/attachments"),
    ("Bad URLs", "item/badurls"),
    ("Moderation", "item/moderation"),
    ("Navigation nodes", "item/navigationNodes"),
    ("History", "item/history"),
    ("Item DIR", "itemdir"),
    ("View IMS", "viewims"),
    ("IMS DIR", "imsdir"),
    ("IMS Package", "item/itembody/packagefile"),
    (DRM_HEADER, "item/rights")
  ).map { case (name, xpath) =>
    CSVHeader(name, xpath)
  }

  /** If a column's header is in this list and its content is formatted as 'name:value', full XML
    * xpath is used as the name. For example, given a XML
    * `<tree><node1><node2>DRM</node2</node1></tree>`, the CSV cell content is
    * `tree/node1/node2:DRM` instead of `node2:DRM`
    */
  val NEED_FULL_XPATH_IN_CONTENT = List[String](
    DRM_HEADER
  )

  val SINGLE_PIPE_DELIMITER = "|"
  val DOUBLE_PIPE_DELIMITER = "||"

  /** Build a list of CSV headers based on Schema.
    * @param schemaNodes
    *   List of Schema nodes which determines what headers to be built
    * @param rootNodeName
    *   Name of root node which is used to build full xpath
    */
  def buildHeadersForSchema(
      schemaNodes: java.util.List[SchemaNode],
      rootNodeName: Option[String]
  ): List[CSVHeader] = {
    def buildFullXpath(path: String): String = {
      rootNodeName match {
        case Some(name) => s"${name}/${path}"
        case None       => path
      }
    }

    schemaNodes.asScala
      .flatMap(node => {
        val xpath = buildFullXpath(node.getName)
        node match {
          case n if n.hasChildren =>
            buildHeadersForSchema(n.getChildNodes, Option(xpath))
          case _ => List(CSVHeader(xpath, xpath))
        }
      })
      .toList
  }

  /** Build a full list of headers which include standard headers and headers generated based on
    * Schema.
    * @param schema
    *   Schema used to generate headers based on its nodes
    */
  def buildCSVHeaders(schema: Schema): List[CSVHeader] =
    STANDARD_HEADER_LIST ++ buildHeadersForSchema(schema.getRootSchemaNode.getChildNodes, None)

  /** Build text based CSV content based on provided XML. Iff a node is a root node and does not
    * have any child nodes or attributes, use only the value as CSV cell content (since we have the
    * node name in the header). For others, the content is a list of strings formatted as
    * `name:value` where `name` may include parent node's name. Each text is separated by a
    * delimiter. When a root node has child nodes, each child node is separated by `||`. For other
    * nodes, use `|` to delimit their attributes, values and child nodes' attributes and values. A
    * typical example is: size:816932|uuid:8353cdfe-8211-4c69-bd94-7784956cebe9|file:cat2.png||
    * size:444808|uuid:c72354f4-0c9f-44f1-93e2-8e7bf050ee5c|file:cat1.jpg
    *
    * @param node
    *   Node for which the content is built
    * @param isRootNode
    *   True if the node is a root node
    * @param parentNodeName
    *   Name of the node's parent node
    */
  def buildCSVCell(
      node: PropBagEx,
      isRootNode: Boolean = true,
      parentNodeName: Option[String] = None
  ): String = {
    val childNodes = node.getChildren.asScala.toList
    val attributes = node.getAttributesForNode("").asScala.toMap
    val delimiter =
      if (isRootNode && childNodes.nonEmpty) DOUBLE_PIPE_DELIMITER else SINGLE_PIPE_DELIMITER
    val parentNameForChildren = parentNodeName.map(name => s"$name/${node.getNodeName}")

    // Process a node's attributes and build a list of text formatted as "key:value".
    def processAttributes: Seq[String] = {
      attributes match {
        case attrMap: Map[String, String] if attrMap.nonEmpty =>
          attrMap.map { case (k, v) =>
            s"${k}:${v}"
          }.toSeq
        case _ => Nil
      }
    }

    // Process a node's value and recursively call 'buildCSVCell' to process child nodes.
    def processValues: Seq[String] = {
      if (childNodes.nonEmpty) {
        childNodes.map(child =>
          buildCSVCell(child, isRootNode = false, parentNodeName = parentNameForChildren)
        )
      } else {
        val nodeValue = node.getNode
        val content = if (isRootNode && childNodes.isEmpty && attributes.isEmpty) {
          nodeValue
        } else {
          s"${parentNameForChildren.getOrElse(node.getNodeName)}:${nodeValue}"
        }
        Seq(content)
      }
    }

    (processValues ++ processAttributes).mkString(delimiter)
  }

  /** Builds a CSV row based on the XML for an Item.
    * @param xml
    *   The XML from an Item for this row
    * @param headers
    *   The CSV column headers
    */
  def buildCSVRow(xml: PropBagEx, headers: List[CSVHeader]): String = {
    // Regex for XPATH that points to an attribute(e.g. item/@name).
    val rowContent          = new StringBuilder
    val xpathAttributeRegex = """^.+/@.+$""".r
    headers.foreach(header => {
      // If the xpath points to an attribute, read the value directly.
      val cellContent: String = header.xpath match {
        case xpathAttributeRegex() => xml.getNode(header.xpath)
        case _ =>
          xml
            .iterator(header.xpath) // This is a PropBagIterator.
            .iterator()             // So we have to call 'iterator' again.
            .asScala
            .map(rootNode => {
              buildCSVCell(
                rootNode,
                parentNodeName = Option(header.name).filter(NEED_FULL_XPATH_IN_CONTENT.contains)
              )
            })
            .toList
            .mkString(SINGLE_PIPE_DELIMITER)
      }

      // Add a comma to separate each cell.
      rowContent.append(StringEscapeUtils.escapeCsv(cellContent))
      rowContent.append(",")
    })
    rowContent.toString()
  }

  def convertSearchResultToXML(searchResults: List[FreetextResult]): List[PropBagEx] = {
    searchResults.map(result => {
      val item                   = LegacyGuice.itemService.getItemPack(result.getItemKey).getItem
      val fullItemXml: PropBagEx = LegacyGuice.itemXsltService.getStandardXmlForXslt(item, null)
      fullItemXml
    })
  }

  /** Write CSV contents into a BufferedOutputStream.
    * @param bos
    *   BufferedOutputStream A BufferedOutputStream wrapping another underlying OutputStream.
    * @param contents
    *   Contents to be written to the BufferedOutputStream.
    */
  def writeRow(bos: BufferedOutputStream, contents: String): Unit = {
    bos.write(s"${contents}\n".getBytes())
    bos.flush()
  }
}
