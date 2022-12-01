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
import { pipe } from "fp-ts/function";
import {
  ACLExpression,
  compactACLExpressions,
  generate,
  generateHumanReadable,
  parse,
  removeRedundantExpressions,
  revertCompactedACLExpressions,
} from "../../../tsrc/modules/ACLExpressionModule";
import { findUserById } from "../../../__mocks__/UserModule.mock";
import { findGroupById } from "../../../__mocks__/GroupModule.mock";
import { findRoleById } from "../../../__mocks__/RoleModule.mock";
import {
  aclEveryone,
  aclEveryoneInfix,
  aclNotUser,
  aclNotUserInfix,
  aclOwner,
  aclOwnerInfix,
  aclThreeItems,
  aclThreeItemsInfix,
  aclTwoItems,
  aclTwoItemsInfix,
  aclUser,
  aclUserInfix,
  aclWithComplexSubExpression,
  aclWithComplexSubExpressionInfix,
  aclWithMultipleSubExpression,
  aclWithMultipleSubExpressionInfix,
  aclWithNestedSubExpression,
  aclWithNestedSubExpressionInfix,
  aclWithSubExpression,
  aclWithSubExpressionInfix,
  childOneItemRedundantExpression,
  childrenItemRedundantExpression,
  childSameOperatorExpression,
  complexExpressionACLExpression,
  complexRedundantExpression,
  everyoneACLExpression,
  notExpression,
  notNestedCompactedExpression,
  notNestedExpression,
  notUnexpectedCompactedExpression,
  notUnexpectedExpression,
  notUnexpectedRevertCompactExpression,
  notUserACLExpression,
  notWithChildCompactedExpression,
  notWithChildExpression,
  ownerACLExpression,
  peerSameOperatorExpression,
  simplifiedChildOneItemRedundantExpression,
  simplifiedChildrenItemRedundantExpression,
  simplifiedChildSameOperatorExpression,
  simplifiedComplexRedundantExpression,
  simplifiedPeerSameOperatorExpression,
  threeItemsACLExpression,
  twoItemsACLExpression,
  userACLExpression,
  withMultipleSubExpression,
  withNestedSubExpressionACLExpression,
  withSubExpressionACLExpression,
} from "../../../__mocks__/ACLExpressionModule.mock";

describe("ACLExpressionModule", () => {
  // convert Either to ACLExpression
  const handleParse = (
    result: E.Either<string[], ACLExpression>
  ): ACLExpression =>
    pipe(
      result,
      E.matchW(
        (errors) => {
          throw Error(errors.toString());
        },
        (result) => result
      )
    );

  describe("parse", () => {
    it("parse text as ACL Expressions", () => {
      expect(parse(aclUser)).toEqual(E.right(userACLExpression));
      expect(parse(aclEveryone)).toEqual(E.right(everyoneACLExpression));
      expect(parse(aclOwner)).toEqual(E.right(ownerACLExpression));
      expect(parse(aclUser)).toEqual(E.right(userACLExpression));
      expect(parse(aclNotUser)).toEqual(E.right(notUserACLExpression));
      expect(parse(aclTwoItems)).toEqual(E.right(twoItemsACLExpression));
      expect(parse(aclThreeItems)).toEqual(E.right(threeItemsACLExpression));
      expect(parse(aclWithSubExpression)).toEqual(
        E.right(withSubExpressionACLExpression)
      );
      expect(parse(aclWithMultipleSubExpression)).toEqual(
        E.right(withMultipleSubExpression)
      );
      expect(parse(aclWithNestedSubExpression)).toEqual(
        E.right(withNestedSubExpressionACLExpression)
      );
      expect(parse(aclWithComplexSubExpression)).toEqual(
        E.right(complexExpressionACLExpression)
      );
    });

    it("text with a blank space at the end should still be able to parsed", () => {
      expect(parse(aclWithComplexSubExpression + " ")).toEqual(
        E.right(complexExpressionACLExpression)
      );
    });
  });

  describe("compactACLExpressions", () => {
    it("lift recipient and children into `NOT` ACLExpression", () => {
      expect(compactACLExpressions(notNestedExpression)).toEqual(
        notNestedCompactedExpression
      );
      expect(compactACLExpressions(notExpression)).toEqual(notExpression);
      expect(compactACLExpressions(notWithChildExpression)).toEqual(
        notWithChildCompactedExpression
      );
      expect(compactACLExpressions(notUnexpectedExpression)).toEqual(
        notUnexpectedCompactedExpression
      );
    });
  });

  describe("revertCompactedACLExpressions", () => {
    it("wrap recipient and children into an `OR` ACLExpression", () => {
      expect(revertCompactedACLExpressions(notExpression)).toEqual(
        notExpression
      );
      expect(
        revertCompactedACLExpressions(notWithChildCompactedExpression)
      ).toEqual(notWithChildExpression);
      expect(
        revertCompactedACLExpressions(notNestedCompactedExpression)
      ).toEqual(notNestedExpression);
      expect(
        revertCompactedACLExpressions(notUnexpectedCompactedExpression)
      ).toEqual(notUnexpectedRevertCompactExpression);
    });

    it("multiple revert actions should still have same results", () => {
      expect(
        pipe(
          notExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(notExpression);
      expect(
        pipe(
          notWithChildCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(notWithChildExpression);
      expect(
        pipe(
          notNestedCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(notNestedExpression);
      expect(
        pipe(
          notUnexpectedCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(notUnexpectedRevertCompactExpression);
    });
  });

  describe("generate", () => {
    it.each([
      ["everyoneACLExpression", everyoneACLExpression, aclEveryone],
      ["ownerACLExpression", ownerACLExpression, aclOwner],
      ["userACLExpression", userACLExpression, aclUser],
      ["notUserACLExpression", notUserACLExpression, aclNotUser],
      ["twoItemsACLExpression", twoItemsACLExpression, aclTwoItems],
      ["threeItemsACLExpression", threeItemsACLExpression, aclThreeItems],
      [
        "withSubExpressionACLExpression",
        withSubExpressionACLExpression,
        aclWithSubExpression,
      ],
      [
        "withMultipleSubExpression",
        withMultipleSubExpression,
        aclWithMultipleSubExpression,
      ],
      [
        "withNestedSubExpressionACLExpression",
        withNestedSubExpressionACLExpression,
        aclWithNestedSubExpression,
      ],
      [
        "complexExpressionACLExpression",
        complexExpressionACLExpression,
        aclWithComplexSubExpression,
      ],
    ])(
      "generate postfix ACL Expression text from %s",
      (_, aclExpression, expectedExpression) => {
        expect(generate(aclExpression)).toEqual(expectedExpression);
      }
    );
  });

  describe("generateHumanReadable", () => {
    /**
     * Generate where mocked versions of functions to look up entities (user, group and role) are provided.
     */
    const generateHumanReadableWithMocks = generateHumanReadable({
      resolveUserProvider: findUserById,
      resolveGroupProvider: findGroupById,
      resolveRoleProvider: findRoleById,
    });

    it.each([
      ["everyoneACLExpression", everyoneACLExpression, aclEveryoneInfix],
      ["ownerACLExpression", ownerACLExpression, aclOwnerInfix],
      ["userACLExpression", userACLExpression, aclUserInfix],
      ["notUserACLExpression", notUserACLExpression, aclNotUserInfix],
      ["twoItemsACLExpression", twoItemsACLExpression, aclTwoItemsInfix],
      ["threeItemsACLExpression", threeItemsACLExpression, aclThreeItemsInfix],
      [
        "withSubExpressionACLExpression",
        withSubExpressionACLExpression,
        aclWithSubExpressionInfix,
      ],
      [
        "withMultipleSubExpression",
        withMultipleSubExpression,
        aclWithMultipleSubExpressionInfix,
      ],
      [
        "withNestedSubExpressionACLExpression",
        withNestedSubExpressionACLExpression,
        aclWithNestedSubExpressionInfix,
      ],
      [
        "complexExpressionACLExpression",
        complexExpressionACLExpression,
        aclWithComplexSubExpressionInfix,
      ],
    ])(
      "generate infix ACL Expression text (human readable text) from %s",
      async (_, aclExpression, expectedExpression) => {
        await expect(
          generateHumanReadableWithMocks(aclExpression)()
        ).resolves.toEqual(expectedExpression);
      }
    );
  });

  describe("removeRedundantExpressions", () => {
    it("remove redundant expressions in objects of ACL Expression", () => {
      expect(removeRedundantExpressions(childSameOperatorExpression)).toEqual(
        simplifiedChildSameOperatorExpression
      );
      expect(
        removeRedundantExpressions(childOneItemRedundantExpression)
      ).toEqual(simplifiedChildOneItemRedundantExpression);
      expect(
        removeRedundantExpressions(childrenItemRedundantExpression)
      ).toEqual(simplifiedChildrenItemRedundantExpression);
      expect(removeRedundantExpressions(peerSameOperatorExpression)).toEqual(
        simplifiedPeerSameOperatorExpression
      );
      expect(removeRedundantExpressions(complexRedundantExpression)).toEqual(
        simplifiedComplexRedundantExpression
      );
    });
  });

  describe("values produced by the module, should also work with the module", () => {
    it.each([
      ["everyone", aclEveryone, everyoneACLExpression],
      ["aclOwner", aclOwner, ownerACLExpression],
      ["aclUser", aclUser, userACLExpression],
      ["aclNotUser", aclNotUser, notUserACLExpression],
      ["aclTwoItems", aclTwoItems, twoItemsACLExpression],
      ["aclThreeItems", aclThreeItems, threeItemsACLExpression],
      [
        "aclWithSubExpression",
        aclWithSubExpression,
        withSubExpressionACLExpression,
      ],
      [
        "aclWithMultipleSubExpression",
        aclWithMultipleSubExpression,
        withMultipleSubExpression,
      ],
      [
        "aclWithNestedSubExpression",
        aclWithNestedSubExpression,
        withNestedSubExpressionACLExpression,
      ],
      [
        "aclWithComplexSubExpression",
        aclWithComplexSubExpression,
        complexExpressionACLExpression,
      ],
    ])("parse -> generate -> parse: %s", (_, acl, expectedACLExpression) => {
      expect(pipe(acl, parse, handleParse, generate, parse)).toEqual(
        E.right(expectedACLExpression)
      );
    });

    it.each([
      ["everyone", everyoneACLExpression, aclEveryone],
      ["aclOwner", ownerACLExpression, aclOwner],
      ["aclUser", userACLExpression, aclUser],
      ["aclNotUser", notUserACLExpression, aclNotUser],
      ["aclTwoItems", twoItemsACLExpression, aclTwoItems],
      ["aclThreeItems", threeItemsACLExpression, aclThreeItems],
      [
        "aclWithSubExpression",
        withSubExpressionACLExpression,
        aclWithSubExpression,
      ],
      [
        "aclWithMultipleSubExpression",
        withMultipleSubExpression,
        aclWithMultipleSubExpression,
      ],
      [
        "aclWithNestedSubExpression",
        withNestedSubExpressionACLExpression,
        aclWithNestedSubExpression,
      ],
      [
        "aclWithComplexSubExpression",
        complexExpressionACLExpression,
        aclWithComplexSubExpression,
      ],
    ])(
      "generate -> parse -> generate: %s",
      (_, aclExpression, expectedText) => {
        expect(
          pipe(aclExpression, generate, parse, handleParse, generate)
        ).toEqual(expectedText);
      }
    );
  });
});
