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
import { SyntheticEvent, useState } from "react";
import { ViewerDefinition } from "../modules/ViewerModule";
import Lightbox from "./Lightbox";

export interface ItemAttachmentLinkProps {
  /**
   * Content to be surrounded byt he link.
   */
  children: React.ReactNode;
  /**
   * Optional description that will be passed to chosen viewer - e.g. Lightbox.
   */
  description?: string;
  /**
   * Optional mimeType that will be passed to chosen viewer - e.g. Lightbox.
   */
  mimeType?: string;
  /**
   * Viewer details for the attachment this link is pointing to. Controls which viewer is triggered
   * when the link is clicked.
   */
  viewerDetails: ViewerDefinition;
}

/**
 * A component to be used for viewing attachments in a uniform manner.
 */
const ItemAttachmentLink = ({
  children,
  description,
  mimeType,
  viewerDetails: [viewer, url],
}: ItemAttachmentLinkProps) => {
  const [showLightbox, setShowLightbox] = useState<boolean>(false);

  let link: JSX.Element = (
    <a href={url} target="_blank" rel="noreferrer">
      {children}
    </a>
  );
  switch (viewer) {
    case "lightbox":
      if (!mimeType) {
        throw new Error(
          "'mimeType' must be specified when viewer is 'lightbox'."
        );
      }
      link = (
        <>
          <a
            onClick={(event: SyntheticEvent) => {
              setShowLightbox(!showLightbox);
              event.stopPropagation();
            }}
          >
            {children}
          </a>
          {showLightbox && ( // minor optimisation to minimise DOM
            <Lightbox
              mimeType={mimeType}
              onClose={() => setShowLightbox(false)}
              open={showLightbox}
              src={url}
              title={description}
            />
          )}
        </>
      );
      break;
    case "link":
      // NOP - go with default which is a link
      break;
    default:
    // NOP - go with default (a simple link) and hope server has provided a suitable link.
    // Will probably need to be further developed when full UI replacement is done.
  }

  return link ?? children;
};

export default ItemAttachmentLink;
