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
var WizardCtrl = {
  setMessage: function (ctrlid, message) {
    var $ctrl = $("#" + ctrlid);
    var $content = $ctrl.children("div:first-child");
    var $msg = $content.children("p.ctrlinvalidmessage");
    if (!message) {
      $content.removeClass("ctrlinvalid");
      $msg.empty();
    } else {
      $content.addClass("ctrlinvalid");
      $msg.html(message);
    }
  },
  affixDiv: function () {
    var ad = $("#affix-div");
    ad.attr("data-spy", "affix");
    var offset = ad.offset().top - 55;
    ad.attr("data-offset-top", offset);
  },
  affixDivNewUI: function () {
    var moderate = $("#moderate");
    var affixDiv = $("#affix-div");
    if (moderate.length > 0) {
      $(window).on("scroll", function () {
        // Use outerHeight to include margin.
        var moderateTop = moderate.outerHeight(true);
        var scrollTop = $(window).scrollTop();
        if (
          scrollTop >= moderateTop &&
          !affixDiv.hasClass("moderation-rightNav")
        ) {
          affixDiv.addClass("moderation-rightNav");
        }
        if (
          scrollTop < moderateTop &&
          affixDiv.hasClass("moderation-rightNav")
        ) {
          affixDiv.removeClass("moderation-rightNav");
        }
      });
    } else {
      affixDiv.addClass("contribution-rightNav");
    }
  },
};
