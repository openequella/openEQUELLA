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

package com.tle.web.freemarker

import freemarker.core.TemplateClassResolver
import freemarker.template.{Template, TemplateException}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.io.{StringReader, StringWriter}
import java.util

/** Guards the FreeMarker SSTI hardening in [[BasicConfiguration]]. This configuration compiles
  * template text that authenticated users can supply (collection summary sections, advanced-script
  * controls, dashboard portlets, MIME display templates), so it must not let a template author
  * instantiate dangerous classes via the ?new() built-in (which would allow OS command execution).
  */
class BasicConfigurationTest extends AnyFunSpec with Matchers with TableDrivenPropertyChecks {

  /** Compiles and evaluates `markup` through a real BasicConfiguration, returning the output.
    *
    * @param markup
    *   the FreeMarker template text to compile and evaluate
    * @param model
    *   the data model exposed to the template; defaults to an empty map
    * @return
    *   the rendered output
    */
  private def render(
      markup: String,
      model: util.Map[String, Object] = new util.HashMap[String, Object]
  ): String = {
    val cfg      = new BasicConfiguration
    val template = new Template("test", new StringReader(markup), cfg)
    val out      = new StringWriter

    template.process(model, out)
    out.toString
  }

  describe("BasicConfiguration FreeMarker hardening") {
    it("sets both hardening properties (SAFER_RESOLVER and ?api disabled)") {
      val cfg = new BasicConfiguration
      cfg.getNewBuiltinClassResolver shouldBe TemplateClassResolver.SAFER_RESOLVER
      cfg.isAPIBuiltinEnabled shouldBe false
    }

    it("rejects dangerous or ineligible classes via ?new()") {
      // Every class a template author could reach for via ?new() must be rejected, whether it is a
      // known FreeMarker code-exec gadget blocked by SAFER_RESOLVER or a class the ?new() built-in
      // refuses because it does not implement TemplateModel.
      val rejectedNewPayloads = Table(
        ("description", "payload"),
        // Execute's constructor/call runs Runtime.exec(...); SAFER_RESOLVER blocks it by name. This is
        // the canonical FreeMarker SSTI -> RCE payload.
        (
          "freemarker.template.utility.Execute (the SSTI -> RCE payload)",
          """<#assign ex="freemarker.template.utility.Execute"?new()>${ex("id")}"""
        ),
        // ObjectConstructor can reflectively instantiate any class; SAFER_RESOLVER blocks it by name.
        (
          "freemarker.template.utility.ObjectConstructor",
          """<#assign oc="freemarker.template.utility.ObjectConstructor"?new()>${oc("java.lang.String")}"""
        ),
        // Blocked by name in SAFER_RESOLVER before the class is even loaded, so this holds whether or
        // not Jython is on the classpath.
        (
          "freemarker.template.utility.JythonRuntime",
          """<#assign jy="freemarker.template.utility.JythonRuntime"?new()>"""
        ),
        // ExecUtils can run OS commands and is the obvious "other useful oEQ class" a reviewer worries
        // about, but ?new() rejects it because it is a plain final utility (private constructor, static
        // exec(...) methods) that does not implement TemplateModel -- even though SAFER_RESOLVER does
        // not list it by name.
        (
          "com.tle.common.util.ExecUtils (a product utility that is not a TemplateModel)",
          """<#assign ex="com.tle.common.util.ExecUtils"?new()>"""
        )
      )

      forAll(rejectedNewPayloads) { (_, payload) =>
        a[TemplateException] should be thrownBy render(payload)
      }
    }

    it("disables the ?api built-in to close the reflection bypass") {
      // Give the template a real object with a usable Java API (a List).
      val model = new util.HashMap[String, Object]
      model.put("items", new java.util.ArrayList[String]())
      a[TemplateException] should be thrownBy render("""${items?api.size()}""", model)
    }

    it("still allows the product's own TemplateModel classes via ?new() (no false positives)") {
      // MutableMapModel is instantiated with ?new() by bundled .ftl templates (e.g. macro/table.ftl)
      // and is not one of the dangerous classes, so SAFER_RESOLVER must still permit it.
      noException should be thrownBy render(
        """<#assign m="com.tle.web.freemarker.MutableMapModel"?new()>"""
      )
    }

    it("renders a plain interpolation unaffected by the hardening") {
      render("Hello ${1 + 1}") shouldBe "Hello 2"
    }
  }
}
