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
import { displayYouTube } from "../../../__stories__/components/Lightbox.stories";
import Lightbox, {
  isLightboxSupportedMimeType,
  LightboxConfig,
} from "../../../tsrc/components/Lightbox";
import { render } from "@testing-library/react";
import { CustomMimeTypes } from "../../../tsrc/modules/MimeTypesModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import "@testing-library/jest-dom/extend-expect";

describe("isLightboxSupportedMimeType", () => {
  it.each<[string, boolean]>([
    ["application/pdf", false],
    ["audio/aac", false],
    ["audio/ogg", true],
    ["image/anything", true],
    ["video/ogg", true],
    ["video/quicktime", false],
    [CustomMimeTypes.YOUTUBE, true],
  ])("MIME type: %s, supported: %s", (mimeType: string, expected: boolean) =>
    expect(isLightboxSupportedMimeType(mimeType)).toEqual(expected)
  );
});

describe("view previous/next attachment", () => {
  const nextImageTitle = "Next image";
  const previousImageTitle = "Previous image";
  const onPrevious = jest.fn().mockReturnValue({
    title: previousImageTitle,
    src: "./thumb.jpg",
    mimeType: "image/jpeg",
  });

  const onNext = jest.fn().mockReturnValue({
    title: nextImageTitle,
    src: "./placeholder-500x500.png",
    mimeType: "image/png",
  });

  it.each([
    [
      "previous",
      {
        mimeType: "image/png",
        src: "./placeholder-135x135.png",
        onPrevious: onPrevious,
      },
      onPrevious,
      languageStrings.lightboxComponent.viewPrevious,
      previousImageTitle,
    ],
    [
      "next",
      {
        src: "./placeholder-500x500.png",
        mimeType: "image/png",
        onNext: onNext,
      },
      onNext,
      languageStrings.lightboxComponent.viewNext,
      nextImageTitle,
    ],
  ])(
    "supports viewing %s attachment",
    (
      which: string,
      config: LightboxConfig,
      handler: jest.Mock,
      buttonText: string,
      updatedTitle: string
    ) => {
      const { getByLabelText, queryByText } = render(
        <Lightbox onClose={jest.fn()} open config={config} />
      );

      userEvent.click(getByLabelText(buttonText));
      expect(handler).toHaveBeenCalledTimes(1);
      expect(queryByText(updatedTitle)).toBeInTheDocument();
    }
  );
});

describe("Support for YouTube videos", () => {
  const youTubeLightboxConfig = displayYouTube.args!.config!;

  it("displays an embedded YouTube player for valid view URL src", () => {
    const { queryByTitle } = render(
      <Lightbox onClose={jest.fn()} open config={youTubeLightboxConfig} />
    );
    expect(
      queryByTitle(languageStrings.youTubePlayer.title)
    ).toBeInTheDocument();
  });

  it("displays an error message if the view URL is invalid", () => {
    const { queryByText } = render(
      <Lightbox
        onClose={jest.fn()}
        open
        config={{
          ...youTubeLightboxConfig,
          src: "https://www.youtube.com/watch/", // missing param v=1234xyz
        }}
      />
    );
    expect(
      queryByText(languageStrings.lightboxComponent.youTubeVideoMissingId)
    ).toBeInTheDocument();
  });
});
