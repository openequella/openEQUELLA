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
import { useEffect, useRef, useState } from "react";

export interface KalturaPlayerEmbedProps {
  dimensions?: {
    /**
     * The height (in pixels) for the embedded Kaltura player.
     */
    height: number;
    /**
     * The width (in pixels) for the embedded Kaltura player.
     */
    width: number;
  };
  /**
   * Kaltura Media Entry ID for the movie, audio, etc to be embedded.
   */
  entryId: string;
  /**
   * A Kaltura Partner ID for the Kaltura account which holds the content identified by `entryId`.
   */
  partnerId: number;
  /**
   * The player `uiconf_id` for the player configuration to be used to create the embedded player.
   */
  uiconfId: number;
}

/**
 * Embeds the specified Kaltura Media Entry (`entryId`) using the specified player configuration
 * (`uiconf_id`). This is achieved by requesting the `embedIframeJs` script from the Kaltura CDN
 * using details of the hosting Kaltura account.
 *
 * When that script is retrieved and embedded in the page, it is then auto executed through the
 * use of the `async` flag on the `script` tag. The execution of the script causes a player to
 * be embedded on the div identified with the `playerId` using `kWidget.embed`.
 *
 * Further resources available at:
 *
 * - Player (`kWidget`) example doco: http://player.kaltura.com/docs/kwidget
 * - Older API doco relating to `kWdiget`: http://player.kaltura.com/docs/api
 *
 * Note: This method is based on the embed code generated via the KMC share/embed functionality.
 *       Although there is the newer doco pointing to the PlayerKitJs, on attempting to use that
 *       a 404 was received. And seeing the KMC is still generating code with embedIframeJs this
 *       seems the correct way.
 */
export const KalturaPlayerEmbed = ({
  dimensions = { width: 400, height: 333 }, // default to value KMC uses when generating embed codes 400 x 333
  entryId,
  partnerId,
  uiconfId,
}: KalturaPlayerEmbedProps) => {
  const divElem = useRef<HTMLElement>();
  const [playerId] = useState<string>(`kaltura_player_${Date.now()}`);

  useEffect(() => {
    if (divElem.current) {
      const src = new URL(
        `https://cdnapisec.kaltura.com/p/${partnerId}/sp/${partnerId}00/embedIframeJs/uiconf_id/${uiconfId}/partner_id/${partnerId}`,
      );
      (
        [
          ["autoembed", true],
          ["entry_id", entryId],
          ["playerId", playerId],
          ["width", dimensions.width],
          ["height", dimensions.height],
        ] as [string, string][]
      ).forEach(([name, value]) => src.searchParams.set(name, value));

      const script = document.createElement("script");
      script.async = true;
      script.src = src.toString();
      // This will result in a <script> block being added below the playerId <div>. Which wil auto
      // execute and then add the player to the playerId `<div>`.
      divElem.current.appendChild(script);
    }
  }, [dimensions, entryId, partnerId, playerId, uiconfId]);

  return (
    <div
      ref={(e) => {
        if (e) {
          divElem.current = e;
        }
      }}
    >
      {/*Player will be embedded to the below div.*/}
      <div
        id={playerId}
        style={{ width: dimensions.width, height: dimensions.height }}
      />
    </div>
  );
};

export default KalturaPlayerEmbed;
