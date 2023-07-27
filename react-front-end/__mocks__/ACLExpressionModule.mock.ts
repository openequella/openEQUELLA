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
import {
  ACLExpression,
  createACLExpression,
} from "../tsrc/modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../tsrc/modules/ACLRecipientModule";
import {
  everyoneRecipient,
  groupStaffRecipient,
  groupStudentRecipient,
  ipRecipient,
  ownerRecipient,
  referRecipient,
  roleGuestRecipient,
  user100Recipient,
  user200Recipient,
  user300Recipient,
  user400Recipient,
  userAdminRecipient,
  userContentAdminRecipient,
} from "./ACLRecipientModule.mock";

export const aclEveryone = ACLRecipientTypes.Everyone;
export const aclEveryoneInfix = "Everyone";

export const aclOwner = ACLRecipientTypes.Owner;
export const aclOwnerInfix = "Owner";

export const aclUser = "U:2";
export const aclUserInfix = "Content Content [ContentAdmin]";

export const aclNotUser = "U:2 NOT";
export const aclNotUserInfix = "NOT Content Content [ContentAdmin]";

export const aclTwoItems = "U:2 U:75abbd62-d91c-4ce5-b4b5-339e0d44ac0e OR";
export const aclTwoItemsInfix =
  "Content Content [ContentAdmin] OR Wat Swindlehurst [admin999]";

export const aclThreeItems =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND";
export const aclThreeItemsInfix =
  "Yasmin Day [user300] AND From 127.0.0.1%2F24 AND From *google*";

export const aclFourItems =
  "U:2 U:75abbd62-d91c-4ce5-b4b5-339e0d44ac0e OR I:127.0.0.1%2F24 OR F:*google* OR";
export const aclFourItemsInfix =
  "Content Content [ContentAdmin] OR Wat Swindlehurst [admin999] OR From 127.0.0.1%2F24 OR From *google*";

export const aclWithSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR AND";
export const aclWithSubExpressionInfix =
  "Yasmin Day [user300] AND From 127.0.0.1%2F24 AND From *google* AND ( Owner OR Guest User Role OR Ronny Southgate [user400] )";

export const aclWithMultipleSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1 OR NOT AND";
export const aclWithMultipleSubExpressionInfix =
  "Yasmin Day [user300] AND From 127.0.0.1%2F24 AND From *google* AND ( Owner OR Guest User Role OR Ronny Southgate [user400] ) AND ( NOT ( Engineering & Computer Science Students OR Engineering & Computer Science Staff ) )";

export const aclWithNestedSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR NOT OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 NOT AND";
export const aclWithNestedSubExpressionInfix =
  "Yasmin Day [user300] AND From 127.0.0.1%2F24 AND From *google* AND ( Owner OR Guest User Role OR Ronny Southgate [user400] OR ( NOT ( Fabienne Hobson [user100] OR Racheal Carlyle [user200] ) ) ) AND ( NOT Engineering & Computer Science Students )";

export const aclWithComplexSubExpression =
  "U:eb75a832-6533-4d72-93f4-2b7a1b108951 I:127.0.0.1%2F24 AND F:*google* AND $OWNER R:TLE_GUEST_USER_ROLE OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef OR U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR NOT OR F:aa F:bb F:cc OR NOT AND OR AND G:99806ac8-410e-4c60-b3ab-22575276f0f0 G:d0265a33-8f89-4cea-8a36-45fd3c4cf5a1 OR NOT AND";
export const aclWithComplexSubExpressionInfix =
  "Yasmin Day [user300] AND From 127.0.0.1%2F24 AND From *google* AND ( Owner OR Guest User Role OR Ronny Southgate [user400] OR ( NOT ( Fabienne Hobson [user100] OR Racheal Carlyle [user200] ) ) OR ( From aa AND ( NOT ( From bb OR From cc ) ) ) ) AND ( NOT ( Engineering & Computer Science Students OR Engineering & Computer Science Staff ) )";

export const everyoneACLExpression: ACLExpression = createACLExpression(
  "OR",
  [everyoneRecipient],
  []
);

export const ownerACLExpression: ACLExpression = createACLExpression(
  "OR",
  [ownerRecipient],
  []
);

export const userACLExpression: ACLExpression = createACLExpression(
  "OR",
  [userContentAdminRecipient],
  []
);

export const notUserACLExpression: ACLExpression = createACLExpression(
  "NOT",
  [userContentAdminRecipient],
  []
);

export const twoItemsACLExpression: ACLExpression = createACLExpression(
  "OR",
  [userContentAdminRecipient, userAdminRecipient],
  []
);

export const threeItemsACLExpression: ACLExpression = createACLExpression(
  "AND",
  [user300Recipient, ipRecipient("127.0.0.1%2F24"), referRecipient("*google*")],
  []
);

export const fourItemsACLExpression: ACLExpression = createACLExpression(
  "OR",
  [
    userContentAdminRecipient,
    userAdminRecipient,
    ipRecipient("127.0.0.1%2F24"),
    referRecipient("*google*"),
  ],
  []
);

export const withSubExpressionACLExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      user300Recipient,
      ipRecipient("127.0.0.1%2F24"),
      referRecipient("*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [ownerRecipient, roleGuestRecipient, user400Recipient],
        []
      ),
    ]
  );

export const withMultipleSubExpression: ACLExpression = createACLExpression(
  "AND",
  [user300Recipient, ipRecipient("127.0.0.1%2F24"), referRecipient("*google*")],
  [
    createACLExpression(
      "OR",
      [ownerRecipient, roleGuestRecipient, user400Recipient],
      []
    ),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [groupStudentRecipient, groupStaffRecipient],
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
      user300Recipient,
      ipRecipient("127.0.0.1%2F24"),
      referRecipient("*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [ownerRecipient, roleGuestRecipient, user400Recipient],
        [
          createACLExpression(
            "NOT",
            [],
            [
              createACLExpression(
                "OR",
                [user100Recipient, user200Recipient],
                []
              ),
            ]
          ),
        ]
      ),
      createACLExpression("NOT", [groupStudentRecipient], []),
    ]
  );

export const complexExpressionACLExpression: ACLExpression =
  createACLExpression(
    "AND",
    [
      user300Recipient,
      ipRecipient("127.0.0.1%2F24"),
      referRecipient("*google*"),
    ],
    [
      createACLExpression(
        "OR",
        [ownerRecipient, roleGuestRecipient, user400Recipient],
        [
          createACLExpression(
            "NOT",
            [],
            [
              createACLExpression(
                "OR",
                [user100Recipient, user200Recipient],
                []
              ),
            ]
          ),
          createACLExpression(
            "AND",
            [referRecipient("aa")],
            [
              createACLExpression(
                "NOT",
                [],
                [
                  createACLExpression(
                    "OR",
                    [referRecipient("bb"), referRecipient("cc")],
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
            [groupStudentRecipient, groupStaffRecipient],
            []
          ),
        ]
      ),
    ]
  );

export const childSameOperatorExpression: ACLExpression = createACLExpression(
  "AND",
  [userContentAdminRecipient],
  [createACLExpression("AND", [groupStudentRecipient, groupStaffRecipient], [])]
);

export const simplifiedChildSameOperatorExpression: ACLExpression =
  createACLExpression(
    "AND",
    [userContentAdminRecipient, groupStudentRecipient, groupStaffRecipient],
    []
  );

export const childOneItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [userContentAdminRecipient],
    [createACLExpression("OR", [groupStudentRecipient], [])]
  );

export const simplifiedChildOneItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [userContentAdminRecipient, groupStudentRecipient],
    []
  );

export const childrenItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [userContentAdminRecipient],
    [
      createACLExpression("OR", [groupStudentRecipient], []),
      createACLExpression("OR", [referRecipient("4")], []),
    ]
  );

export const simplifiedChildrenItemRedundantExpression: ACLExpression =
  createACLExpression(
    "AND",
    [userContentAdminRecipient, groupStudentRecipient, referRecipient("4")],
    []
  );

export const peerSameOperatorExpression: ACLExpression = createACLExpression(
  "OR",
  [userContentAdminRecipient],
  [
    createACLExpression("OR", [groupStudentRecipient], []),
    createACLExpression("AND", [referRecipient("4")], []),
    createACLExpression(
      "AND",
      [referRecipient("10"), referRecipient("11")],
      []
    ),
    createACLExpression("OR", [referRecipient("5"), referRecipient("6")], []),
    createACLExpression("NOT", [referRecipient("7")], []),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [referRecipient("8"), referRecipient("9")],
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
      userContentAdminRecipient,
      groupStudentRecipient,
      referRecipient("4"),
      referRecipient("5"),
      referRecipient("6"),
    ],
    [
      createACLExpression(
        "AND",
        [referRecipient("10"), referRecipient("11")],
        []
      ),
      createACLExpression("NOT", [referRecipient("7")], []),
      createACLExpression(
        "NOT",
        [],
        [
          createACLExpression(
            "OR",
            [referRecipient("8"), referRecipient("9")],
            []
          ),
        ]
      ),
    ]
  );

export const complexRedundantExpression: ACLExpression = createACLExpression(
  "OR",
  [referRecipient("U1"), referRecipient("G1")],
  [
    createACLExpression("AND", [], []),
    createACLExpression(
      "OR",
      [userContentAdminRecipient, referRecipient("3")],
      [
        createACLExpression("OR", [], []),
        createACLExpression("AND", [referRecipient("4")], []),
      ]
    ),
    createACLExpression("AND", [], []),
    createACLExpression(
      "NOT",
      [],
      [
        createACLExpression(
          "OR",
          [referRecipient("2"), referRecipient("3")],
          [
            createACLExpression(
              "OR",
              [referRecipient("aa"), referRecipient("bb")],
              []
            ),
            createACLExpression(
              "AND",
              [referRecipient("cc"), referRecipient("dd")],
              []
            ),
            createACLExpression(
              "NOT",
              [referRecipient("ee"), referRecipient("ff")],
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
      referRecipient("U1"),
      referRecipient("G1"),
      userContentAdminRecipient,
      referRecipient("3"),
      referRecipient("4"),
    ],
    [
      createACLExpression(
        "NOT",
        [],
        [
          createACLExpression(
            "OR",
            [
              referRecipient("2"),
              referRecipient("3"),
              referRecipient("aa"),
              referRecipient("bb"),
            ],
            [
              createACLExpression(
                "AND",
                [referRecipient("cc"), referRecipient("dd")],
                []
              ),
              createACLExpression(
                "NOT",
                [referRecipient("ee"), referRecipient("ff")],
                []
              ),
            ]
          ),
        ]
      ),
    ]
  );

export const emptyRecipientWithOneChildExpression: ACLExpression =
  createACLExpression(
    "AND",
    [],
    [createACLExpression("OR", [referRecipient("a"), referRecipient("b")], [])]
  );
export const simplifiedEmptyRecipientWithOneChildExpression: ACLExpression =
  createACLExpression("OR", [referRecipient("a"), referRecipient("b")], []);

export const nestedEmptyRecipientWithOneChildExpression: ACLExpression =
  createACLExpression(
    "AND",
    [],
    [
      createACLExpression(
        "OR",
        [referRecipient("a"), referRecipient("b")],
        [
          createACLExpression(
            "AND",
            [],
            [
              createACLExpression(
                "OR",
                [referRecipient("c"), referRecipient("d")],
                []
              ),
            ]
          ),
        ]
      ),
    ]
  );
export const simplifiedNestedEmptyRecipientWithOneChildExpression: ACLExpression =
  createACLExpression(
    "OR",
    [
      referRecipient("a"),
      referRecipient("b"),
      referRecipient("c"),
      referRecipient("d"),
    ],
    []
  );

/**
 * ```
 * NOT
 *   F:A
 * ```
 * */
export const notExpression: ACLExpression = createACLExpression("NOT", [
  referRecipient("A"),
]);

/**
 * ```
 * NOT
 *   OR
 *     F:A F:B
 *     AND
 *       F:C F:D
 * ```
 * */
export const notWithChildExpression: ACLExpression = createACLExpression(
  "NOT",
  [],
  [
    createACLExpression(
      "OR",
      [referRecipient("A"), referRecipient("B")],
      [createACLExpression("AND", [referRecipient("C"), referRecipient("D")])]
    ),
  ]
);

/**
 * ```
 * NOT
 *   F:A F:B
 *   AND
 *     F:C F:D
 * ```
 * */
export const notWithChildCompactedExpression: ACLExpression =
  createACLExpression(
    "NOT",
    [referRecipient("A"), referRecipient("B")],
    [createACLExpression("AND", [referRecipient("C"), referRecipient("D")])]
  );

/**
 * ```
 * NOT
 *   OR
 *     F:A F:B
 *     AND
 *       F:C F:D
 *       NOT
 *         OR
 *           F:E F:F
 *           NOT
 *               F:G
 * ```
 * */
export const notNestedExpression: ACLExpression = createACLExpression(
  "NOT",
  [],
  [
    createACLExpression(
      "OR",
      [referRecipient("A"), referRecipient("B")],
      [
        createACLExpression(
          "AND",
          [referRecipient("C"), referRecipient("D")],
          [
            createACLExpression(
              "NOT",
              [],
              [
                createACLExpression(
                  "OR",
                  [referRecipient("E"), referRecipient("F")],
                  [createACLExpression("NOT", [referRecipient("G")])]
                ),
              ]
            ),
          ]
        ),
      ]
    ),
  ]
);

/**
 * ```
 * NOT
 *   F:A F:B
 *   AND
 *     F:C F:D
 *     NOT
 *         F:E F:F
 *         NOT
 *           F:G
 * ```
 */
export const notNestedCompactedExpression: ACLExpression = createACLExpression(
  "NOT",
  [referRecipient("A"), referRecipient("B")],
  [
    createACLExpression(
      "AND",
      [referRecipient("C"), referRecipient("D")],
      [
        createACLExpression(
          "NOT",
          [referRecipient("E"), referRecipient("F")],
          [createACLExpression("NOT", [referRecipient("G")])]
        ),
      ]
    ),
  ]
);

/**
 * ```
 * NOT
 *   F:1 F:2
 *   OR
 *     F:A F:B
 *     AND
 *       F:C F:D
 *   AND
 *     F:E F:F
 * ```
 * */
export const notUnexpectedExpression: ACLExpression = createACLExpression(
  "NOT",
  [referRecipient("1"), referRecipient("2")],
  [
    createACLExpression(
      "OR",
      [referRecipient("A"), referRecipient("B")],
      [createACLExpression("AND", [referRecipient("C"), referRecipient("D")])]
    ),
    createACLExpression("AND", [referRecipient("E"), referRecipient("F")]),
  ]
);

/**
 * ```
 * NOT
 *   F:1 F:2 F:A F:B F:E F:F
 *   AND
 *     F:C F:D
 * ```
 * */
export const notUnexpectedCompactedExpression: ACLExpression =
  createACLExpression(
    "NOT",
    [
      referRecipient("1"),
      referRecipient("2"),
      referRecipient("A"),
      referRecipient("B"),
      referRecipient("E"),
      referRecipient("F"),
    ],
    [createACLExpression("AND", [referRecipient("C"), referRecipient("D")])]
  );

/**
 * ```
 * NOT
 *   OR
 *      F:1 F:2 F:A F:B
 *     AND
 *       F:C F:D
 *     AND
 *       F:E F:F
 * ```
 * */
export const notUnexpectedRevertCompactExpression: ACLExpression =
  createACLExpression(
    "NOT",
    [],
    [
      createACLExpression(
        "OR",
        [
          referRecipient("1"),
          referRecipient("2"),
          referRecipient("A"),
          referRecipient("B"),
          referRecipient("E"),
          referRecipient("F"),
        ],
        [createACLExpression("AND", [referRecipient("C"), referRecipient("D")])]
      ),
    ]
  );
