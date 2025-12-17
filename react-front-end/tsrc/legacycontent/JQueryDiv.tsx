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
import * as React from "react";

export interface JQueryDivProps extends React.HTMLAttributes<HTMLDivElement> {
  html: string;
  children?: never;
}

/**
 * Provides a means to have a `div` whose content is the raw HTML returned from the server. Also
 * used to support the various JQuery AJAX stuff partially due to the clean-up effect of emptying
 * out the `div`.
 *
 * It would be nice if we could get away with a simpler:
 *
 * `<div {...withoutOthers} dangerouslySetInnerHTML={{ __html: html}}/>`
 *
 * However that does not work, using jQuery to set the content of the `div` and then especially
 * clearing the `div` in the clean-up effect is required for some edge cases. Why, is not entirely
 * clear.
 */
const JQueryDiv = React.memo(({ html, ...withoutOthers }: JQueryDivProps) => {
  const divElem = React.useRef<HTMLElement>(null);

  // Just a clean-up effect to clear out the div at un-mount time.
  // This is key for supporting the AJAXy stuff.
  React.useEffect(
    () => () => {
      if (divElem.current) {
        $(divElem.current).empty();
      }
    },
    [],
  );

  return (
    <div
      {...withoutOthers}
      ref={(e) => {
        if (e) {
          divElem.current = e;
          $(e).html(html);
        }
      }}
    />
  );
});

export default JQueryDiv;
