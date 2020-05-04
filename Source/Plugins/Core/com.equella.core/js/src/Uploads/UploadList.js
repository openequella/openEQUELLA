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
exports.updateCtrlErrorText = function (ctrlId, text) {
  var contElem = document.querySelector("DIV#" + ctrlId + " > DIV.control");
  if (text == "") contElem.classList.remove("ctrlinvalid");
  else contElem.classList.add("ctrlinvalid");
  contElem.querySelector("P.ctrlinvalidmessage").textContent = text;
};

exports.updateDuplicateMessage = function (id, display) {
  // The div id of all duplicate warning messages automatically follows
  // this format: its parent div id concatenated with "_duplicateWarningMessage"
  var duplicateMessageDiv = document.querySelector(
    "#" + id + "_attachment_duplicate_warning"
  );
  if (duplicateMessageDiv != null) {
    if (display) {
      duplicateMessageDiv.setAttribute("style", "display:inline");
    } else {
      duplicateMessageDiv.setAttribute("style", "display:none");
    }
  }
};

exports.simpleFormat = function (format) {
  return function (args) {
    return format.replace(/{(\d+)}/g, function (match, number) {
      return typeof args[number] != "undefined" ? args[number] : match;
    });
  };
};

exports.register = function (exp) {
  window.UploadList = exp;
};
