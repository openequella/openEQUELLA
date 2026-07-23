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

package com.tle.web.scripting.objects

import com.tle.common.util.ExecUtils
import com.tle.common.util.ExecUtils.ExecResult
import com.tle.core.services.FileSystemService
import com.tle.exceptions.AccessDeniedException
import org.mockito.Mockito.{mock, mockStatic, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** Since the result of java.io.File#getCanonicalPath() is OS dependent, and we only use Linux
  * runners in GitLab CI pipelines, this test only covers Linux use cases.
  */
class SystemScriptWrapperTest extends AnyFunSpec with Matchers {

  private val mockFileSystemService: FileSystemService = mock(classOf[FileSystemService])

  private def wrapperWithAllowlist(allowedExecutablesConfig: String): SystemScriptWrapper = {
    new SystemScriptWrapper(mockFileSystemService, allowedExecutablesConfig)
  }

  describe("SystemScriptWrapper.setAllowedExecutablesConfig") {
    it("excludes a configured entry that cannot be resolved to a canonical path") {
      val unresolvablePath = "/bin/sh" + 0.toChar + "evil"
      val blankPath        = "   "
      val wrapper          = wrapperWithAllowlist(s"/bin/sh,$unresolvablePath,$blankPath")

      wrapper.getAllowedExecutables should not contain unresolvablePath
      wrapper.getAllowedExecutables should not contain blankPath
    }
  }

  describe("SystemScriptWrapper.execute") {
    it("rejects every executable when the allow-list is empty") {
      val wrapper = wrapperWithAllowlist("")
      assertThrows[AccessDeniedException] {
        wrapper.execute("/bin/sh", Array[AnyRef]())
      }
    }

    it("rejects an executable that is not on the allow-list") {
      val wrapper = wrapperWithAllowlist("/bin/sh")
      assertThrows[AccessDeniedException] {
        wrapper.execute("/bin/pwd", Array[AnyRef]("."))
      }
    }

    it("runs the command via ExecUtils when the executable is on the allow-list") {
      val wrapper         = wrapperWithAllowlist("/bin/sh")
      val expectedCommand = Array("/bin/sh", "-c", "true")

      mockStatic(classOf[ExecUtils])
      when(ExecUtils.exec(expectedCommand: _*)).thenReturn(new ExecResult(0, "ok", ""))

      val result = wrapper.execute("/bin/sh", Array[AnyRef]("-c", "true"))
      result.getCode shouldBe 0
    }
  }

  describe("SystemScriptWrapper.executeInBackground") {
    it("rejects every executable when the allow-list is empty") {
      val wrapper = wrapperWithAllowlist("")
      assertThrows[AccessDeniedException] {
        wrapper.executeInBackground("/bin/sh", Array[AnyRef]())
      }
    }

    it("rejects an executable that is not on the allow-list before spawning a thread") {
      val wrapper = wrapperWithAllowlist("/bin/sh")
      assertThrows[AccessDeniedException] {
        wrapper.executeInBackground("/bin/pwd", Array[AnyRef]("."))
      }
    }

    it("does not throw when the executable is on the allow-list") {
      val wrapper = wrapperWithAllowlist("/bin/sh")
      noException should be thrownBy {
        wrapper.executeInBackground("/bin/sh", Array[AnyRef]("-c", "true"))
      }
    }
  }
}
