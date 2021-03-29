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
import { Link } from "@material-ui/core";
import * as React from "react";
import { SyntheticEvent, useState } from "react";
import {
  AttachmentAndViewerConfig,
  isViewerLightboxConfig,
  ViewerLightboxConfig,
} from "../modules/ViewerModule";
import { languageStrings } from "../util/langstrings";
import Lightbox, { LightboxProps } from "./Lightbox";

export interface ItemAttachmentLinkProps {
  /**
   * Content to be surrounded by the link.
   */
  children: React.ReactNode;
  /**
   * Information about an attachment and what viewer to be used for this attachment.
   */
  selectedAttachment: AttachmentAndViewerConfig;
}

/**
 * A component to be used for viewing attachments in a uniform manner. If the viewer specified
 * in `viewerDetails` is anything other than `lightbox` then a simple link will be created. In
 * future versions of oEQ when the balance of New UI is undertaken, then in theory this component
 * will need to handle the other types of viewers - e.g. generating links for google docs, or
 * downloading attachments, etc.
 */
const ItemAttachmentLink = ({
  children,
  selectedAttachment: {
    attachment: { description, mimeType },
    viewerConfig,
  },
}: ItemAttachmentLinkProps) => {
  const { attachmentLink } = languageStrings.searchpage.searchResult;
  const [lightBoxProps, setLightBoxProps] = useState<LightboxProps>();

  const buildLightboxLink = ({ config }: ViewerLightboxConfig): JSX.Element => {
    if (!mimeType) {
      throw new Error(
        "'mimeType' must be specified when viewer is 'lightbox'."
      );
    }

    return (
      <>
        <Link
          aria-label={`${attachmentLink} ${description}`}
          component="button"
          onClick={(event: SyntheticEvent) => {
            setLightBoxProps({
              open: true,
              onClose: () => {
                setLightBoxProps(undefined);
              },
              config,
            });
            event.stopPropagation();
          }}
        >
          {children}
        </Link>
        {
          lightBoxProps && <Lightbox {...lightBoxProps} /> // minor optimisation to minimise DOM
        }
      </>
    );
  };
  return isViewerLightboxConfig(viewerConfig) ? (
    buildLightboxLink(viewerConfig)
  ) : (
    // Lightbox viewer not specified, so go with the default of a simple link.
    <Link
      aria-label={`${attachmentLink} ${description}`}
      href={viewerConfig.url}
      target="_blank"
      rel="noreferrer"
    >
      {children}
    </Link>
  );
};

export default ItemAttachmentLink;
