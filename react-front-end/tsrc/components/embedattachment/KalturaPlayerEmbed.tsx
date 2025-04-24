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
import {
  buildKalturaPlayerUrl,
  KalturaPlayerDetails,
} from "../../modules/KalturaModule";

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
  playerDetails: KalturaPlayerDetails;
}

/**
 * Embeds the specified Kaltura Media Entry (`entryId`) using the AUTO Embed style. This is achieved
 * by requesting the `embedPlaykitJs ` script with the specified player configuration (`uiconf_id`)
 * from the Kaltura CDN using details of the hosting Kaltura account.
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
  dimensions = { width: 560, height: 395 }, // default to the standard V7 player dimensions
  playerDetails,
}: KalturaPlayerEmbedProps) => {
  const divElem = useRef<HTMLElement>();
  const [playerId] = useState<string>(`kaltura_player_${Date.now()}`);

  useEffect(() => {
    if (divElem.current) {
      const script = document.createElement("script");
      script.async = true;
      script.src = buildKalturaPlayerUrl(playerDetails, playerId, "AUTO");
      // This will result in a <script> block being added below the playerId <div>. Which wil auto
      // execute and then add the player to the playerId `<div>`.
      divElem.current.appendChild(script);
    }
  }, [dimensions, playerDetails, playerId]);

  return (
    <div
      ref={(e) => {
        if (e) {
          divElem.current = e;
        }
      }}
    >
      {/*Player will be embedded to the below div.*/}
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events,jsx-a11y/no-static-element-interactions */}
      <div
        id={playerId}
        style={{ width: dimensions.width, height: dimensions.height }}
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
};

export default KalturaPlayerEmbed;
