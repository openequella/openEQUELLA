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
import * as E from "fp-ts/Either";
import { identity, pipe } from "fp-ts/function";
import {
  ACLExpression,
  ACLRecipient,
  ACLRecipientTypes,
  createACLExpression,
  createACLRecipientEither,
} from "../tsrc/modules/ACLExpressionModule";

export const aclEveryone = ACLRecipientTypes.Everyone;
export const aclOwner = ACLRecipientTypes.Owner;
export const aclUser = "U:2";
export const aclGroup = "G:e810bee1-f2da-4145-8bc3-dc6fec827429";
export const aclRole = "R:e8a88448-cdeb-43d0-afc6-8c491266271a";
export const aclRefer = "F:*https://google.com/example*";
export const aclExactRefer = "F:https://google.com/example";
export const aclIp = "I:123.123.0.0%2F32";
export const aclSso = "T:moodle";

export const aclNotUser = "U:2 NOT";
export const aclNotUserInfix = "NOT ( U:2 )";

export const aclTwoItems = "U:2 U:75abbd62-d91c-4ce5-b4b5-339e0d44ac0e OR";

export const aclThreeItems =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND";

export const aclWithSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR AND";

export const aclWithMultipleSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1 OR NOT AND";

export const aclWithNestedSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR NOT OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 NOT AND";

export const aclWithComplexSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR NOT OR F:aa F:bb F:cc OR NOT AND OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1 OR NOT AND";

const createACLRecipient = (expression: string): ACLRecipient =>
  pipe(
    expression,
    createACLRecipientEither,
    E.match((message) => {
      throw TypeError(message);
    }, identity)
  );

export const everyoneACLExpression: ACLExpression = createACLExpression(
  "OR",
  [createACLRecipient(aclEveryone)],
  []
);

export const ownerACLExpression: ACLExpression = createACLExpression(
  "OR",
  [createACLRecipient(aclOwner)],
  []
);

export const userACLExpression: ACLExpression = createACLExpression(
  "OR",
  [createACLRecipient(aclUser)],
  []
);

export const notUserACLExpression: ACLExpression = createACLExpression(
  "NOT",
  [createACLRecipient("U:2")],
  []
);

export const twoItemsACLExpression: ACLExpression = createACLExpression(
  "OR",
  [
    createACLRecipient("U:2"),
    createACLRecipient("U:75abbd62-d91c-4ce5-b4b5-339e0d44ac0e"),
  ],
  []
);

export const threeItemsACLExpression: ACLExpression = createACLExpression(
  "AND",
  [
    createACLRecipient("U:eb75a832-6533-4d72-93f4-2b7a1b108951"),
    createACLRecipient("I:127.0.0.1%2F24"),
    createACLRecipient("F:*google*"),
  ],
  []
);

export const withSubExpressionACLExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:eb75a832-6533-4d72-93f4-2b7a1b108951"),
      createACLRecipient("I:127.0.0.1%2F24"),
      createACLRecipient("F:*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [
          createACLRecipient("$OWNER"),
          createACLRecipient("R:TLE_GUEST_USER_ROLE"),
          createACLRecipient("U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef"),
        ],
        []
      ),
    ]
  );

export const withMultipleSubExpression: ACLExpression = createACLExpression(
  "AND",
  [
    createACLRecipient("U:eb75a832-6533-4d72-93f4-2b7a1b108951"),
    createACLRecipient("I:127.0.0.1%2F24"),
    createACLRecipient("F:*google*"),
  ],
  [
    createACLExpression(
      "OR",
      [
        createACLRecipient("$OWNER"),
        createACLRecipient("R:TLE_GUEST_USER_ROLE"),
        createACLRecipient("U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef"),
      ],
      []
    ),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [
            createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
            createACLRecipient("G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1"),
          ],
          []
        ),
      ]
    ),
  ]
);

export const withNestedSubExpressionACLExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:eb75a832-6533-4d72-93f4-2b7a1b108951"),
      createACLRecipient("I:127.0.0.1%2F24"),
      createACLRecipient("F:*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [
          createACLRecipient("$OWNER"),
          createACLRecipient("R:TLE_GUEST_USER_ROLE"),
          createACLRecipient("U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef"),
        ],
        [
          createACLExpression(
            "NOT",
            [],
            [
              createACLExpression(
                "OR",
                [
                  createACLRecipient("U:20483af2-fe56-4499-a54b-8d7452156895"),
                  createACLRecipient("U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a"),
                ],
                []
              ),
            ]
          ),
        ]
      ),
      createACLExpression(
        "NOT",
        [createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0")],
        []
      ),
    ]
  );

export const complexExpressionACLExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:eb75a832-6533-4d72-93f4-2b7a1b108951"),
      createACLRecipient("I:127.0.0.1%2F24"),
      createACLRecipient("F:*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [
          createACLRecipient("$OWNER"),
          createACLRecipient("R:TLE_GUEST_USER_ROLE"),
          createACLRecipient("U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef"),
        ],
        [
          createACLExpression(
            "NOT",
            [],
            [
              createACLExpression(
                "OR",
                [
                  createACLRecipient("U:20483af2-fe56-4499-a54b-8d7452156895"),
                  createACLRecipient("U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a"),
                ],
                []
              ),
            ]
          ),
          createACLExpression(
            "AND",
            [createACLRecipient("F:aa")],
            [
              createACLExpression(
                "NOT",
                [],
                [
                  createACLExpression(
                    "OR",
                    [createACLRecipient("F:bb"), createACLRecipient("F:cc")],
                    []
                  ),
                ]
              ),
            ]
          ),
        ]
      ),
      createACLExpression(
        "NOT",
        [],
        [
          createACLExpression(
            "OR",
            [
              createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
              createACLRecipient("G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1"),
            ],
            []
          ),
        ]
      ),
    ]
  );

export const childSameOperatorExpression: ACLExpression = createACLExpression(
  "AND",
  [createACLRecipient("U:2")],
  [
    createACLExpression(
      "AND",
      [
        createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
        createACLRecipient("G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1"),
      ],
      []
    ),
  ]
);

export const simplifiedChildSameOperatorExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:2"),
      createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
      createACLRecipient("G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1"),
    ],
    []
  );

export const childOneItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [createACLRecipient("U:2")],
    [
      createACLExpression(
        "OR",
        [createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0")],
        []
      ),
    ]
  );

export const simplifiedChildOneItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:2"),
      createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
    ],
    []
  );

export const childrenItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [createACLRecipient("U:2")],
    [
      createACLExpression(
        "OR",
        [createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0")],
        []
      ),
      createACLExpression("OR", [createACLRecipient("G:4")], []),
    ]
  );

export const simplifiedChildrenItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      createACLRecipient("U:2"),
      createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
      createACLRecipient("G:4"),
    ],
    []
  );

export const peerSameOperatorExpression: ACLExpression = createACLExpression(
  "OR",
  [createACLRecipient("U:2")],
  [
    createACLExpression(
      "OR",
      [createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0")],
      []
    ),
    createACLExpression("AND", [createACLRecipient("G:4")], []),
    createACLExpression(
      "AND",
      [createACLRecipient("G:10"), createACLRecipient("G:11")],
      []
    ),
    createACLExpression(
      "OR",
      [createACLRecipient("G:5"), createACLRecipient("G:6")],
      []
    ),
    createACLExpression("NOT", [createACLRecipient("G:7")], []),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [createACLRecipient("G:8"), createACLRecipient("G:9")],
          []
        ),
      ]
    ),
  ]
);

export const simplifiedPeerSameOperatorExpression: ACLExpression =
  createACLExpression(
    "OR",
    [
      createACLRecipient("U:2"),
      createACLRecipient("G:99806ac8-410e-4c60-b3ab-22575276f0f0"),
      createACLRecipient("G:4"),
      createACLRecipient("G:5"),
      createACLRecipient("G:6"),
    ],
    [
      createACLExpression(
        "AND",
        [createACLRecipient("G:10"), createACLRecipient("G:11")],
        []
      ),
      createACLExpression("NOT", [createACLRecipient("G:7")], []),
      createACLExpression(
        "NOT",
        [],
        [
          createACLExpression(
            "OR",
            [createACLRecipient("G:8"), createACLRecipient("G:9")],
            []
          ),
        ]
      ),
    ]
  );

export const complexRedundantExpression: ACLExpression = createACLExpression(
  "OR",
  [createACLRecipient("U:1"), createACLRecipient("G:1")],
  [
    createACLExpression("AND", [], []),
    createACLExpression(
      "OR",
      [createACLRecipient("U:2"), createACLRecipient("U:3")],
      [
        createACLExpression("OR", [], []),
        createACLExpression("AND", [createACLRecipient("U:4")], []),
      ]
    ),
    createACLExpression("AND", [], []),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [createACLRecipient("G:2"), createACLRecipient("G:3")],
          [
            createACLExpression(
              "OR",
              [createACLRecipient("F:aa"), createACLRecipient("F:bb")],
              []
            ),
            createACLExpression(
              "AND",
              [createACLRecipient("F:cc"), createACLRecipient("F:dd")],
              []
            ),
            createACLExpression(
              "NOT",
              [createACLRecipient("F:ee"), createACLRecipient("F:ff")],
              []
            ),
          ]
        ),
      ]
    ),
  ]
);

export const simplifiedComplexRedundantExpression: ACLExpression =
  createACLExpression(
    "OR",
    [
      createACLRecipient("U:1"),
      createACLRecipient("G:1"),
      createACLRecipient("U:2"),
      createACLRecipient("U:3"),
      createACLRecipient("U:4"),
    ],
    [
      createACLExpression(
        "NOT",
        [],
        [
          createACLExpression(
            "OR",
            [
              createACLRecipient("G:2"),
              createACLRecipient("G:3"),
              createACLRecipient("F:aa"),
              createACLRecipient("F:bb"),
            ],
            [
              createACLExpression(
                "AND",
                [createACLRecipient("F:cc"), createACLRecipient("F:dd")],
                []
              ),
              createACLExpression(
                "NOT",
                [createACLRecipient("F:ee"), createACLRecipient("F:ff")],
                []
              ),
            ]
          ),
        ]
      ),
    ]
  );
