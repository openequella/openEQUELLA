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
import userEvent from "@testing-library/user-event";
import * as React from "react";
import Lightbox, {
  isLightboxSupportedMimeType,
} from "../../../tsrc/components/Lightbox";
import { render } from "@testing-library/react";
import { languageStrings } from "../../../tsrc/util/langstrings";

describe("isLightboxSupportedMimeType", () => {
  it.each<[string, boolean]>([
    ["application/pdf", false],
    ["audio/aac", false],
    ["audio/ogg", true],
    ["image/anything", true],
    ["video/ogg", true],
    ["video/quicktime", false],
  ])("MIME type: %s, supported: %s", (mimeType: string, expected: boolean) =>
    expect(isLightboxSupportedMimeType(mimeType)).toEqual(expected)
  );

  it("supports viewing previous/next attachment", () => {
    const onPrevious = jest.fn();
    const onNext = jest.fn();
    const { getByLabelText } = render(
      <Lightbox
        mimeType="image/png"
        onClose={jest.fn()}
        open
        src="./placeholder-135x135.png"
        onNext={onNext}
        onPrevious={onPrevious}
      />
    );

    const previousButton = getByLabelText(
      languageStrings.lightboxComponent.viewPrevious
    );
    userEvent.click(previousButton);
    expect(onPrevious).toHaveBeenCalledTimes(1);

    const nextButton = getByLabelText(
      languageStrings.lightboxComponent.viewNext
    );
    userEvent.click(nextButton);
    expect(onNext).toHaveBeenCalledTimes(1);
  });
});
