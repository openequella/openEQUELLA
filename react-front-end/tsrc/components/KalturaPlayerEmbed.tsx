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
import { KalturaPlayerVersion } from "../modules/KalturaModule";

export interface KalturaPlayerEmbedProps {
  /**
   * The height (in pixels) for the embedded Kaltura player.
   */
  height: number;
  /**
   * The width (in pixels) for the embedded Kaltura player.
   */
  width: number;
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
  /**
   * Version of the selected player to use. Must be either "V2" or "V7".
   */
  version: KalturaPlayerVersion;
}

/**
 * Embeds the specified Kaltura Media Entry (`entryId`) using the specified player configuration
 * (`uiconf_id`). This is achieved by requesting the `embedPlaykitJs ` script from the Kaltura CDN
 * using details of the hosting Kaltura account.
 *
 * When that script is retrieved and embedded in the page, it is then auto executed through the
 * use of the `async` flag on the `script` tag. The execution of the script causes a player to
 * be embedded on the div identified with the `playerId` using `KalturaPlayer.setup`.
 *
 * Further resources available at:
 *
 * - https://knowledge.kaltura.com/help/player-embed
 * - https://github.com/kaltura/kaltura-player-js/blob/mwEmbed-vs-playkitjs/docs/mwembed-playkitjs-parity.md
 */
export const KalturaPlayerEmbed = ({
  width,
  height,
  entryId,
  partnerId,
  uiconfId,
  version,
}: KalturaPlayerEmbedProps) => {
  const divElem = useRef<HTMLElement>();
  const [playerId] = useState<string>(`kaltura_player_${Date.now()}`);

  useEffect(() => {
    if (divElem.current) {
      const buildPlayerUrl = (): string => {
        const src = new URL(
          version === "V7"
            ? `https://cdnapisec.kaltura.com/p/${partnerId}/embedPlaykitJs/uiconf_id/${uiconfId}`
            : `https://cdnapisec.kaltura.com/p/${partnerId}/sp/${partnerId}00/embedIframeJs/uiconf_id/${uiconfId}/partner_id/${partnerId}`,
        );
        (
          [
            ["autoembed", true],
            ["entry_id", entryId],
            [version === "V7" ? "targetId" : "playerId", playerId],
            ["width", width],
            ["height", height],
          ] as [string, string][]
        ).forEach(([name, value]) => src.searchParams.set(name, value));

        return src.toString();
      };

      const script = document.createElement("script");
      script.async = true;
      script.src = buildPlayerUrl();
      // This will result in a <script> block being added below the playerId <div>. Which wil auto
      // execute and then add the player to the playerId `<div>`.
      divElem.current.appendChild(script);
    }
  }, [width, height, entryId, partnerId, playerId, uiconfId, version]);

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
        style={{ width, height }}
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
};

export default KalturaPlayerEmbed;
