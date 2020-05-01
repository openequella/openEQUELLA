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
  script?: string;
  afterHtml?: () => void;
  children?: never;
}

export default React.memo(function JQueryDiv(props: JQueryDivProps) {
  const divElem = React.useRef<HTMLElement>();
  React.useEffect(
    () => () => {
      if (divElem.current) {
        $(divElem.current).empty();
      }
    },
    []
  );
  const withoutOthers = {
    ...props,
  };
  delete withoutOthers.afterHtml;
  delete withoutOthers.script;
  delete withoutOthers.html;
  return (
    <div
      {...withoutOthers}
      ref={(e) => {
        if (e) {
          divElem.current = e;
          $(e).html(props.html);
          if (props.script) window.eval(props.script);
          if (props.afterHtml) props.afterHtml();
        }
      }}
    />
  );
});
