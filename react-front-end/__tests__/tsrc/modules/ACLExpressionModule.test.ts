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
import * as O from "fp-ts/Option";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import { initialACLExpression } from "../../../__mocks__/ACLExpressionBuilder.mock";
import {
  user100Recipient,
  user200Recipient,
} from "../../../__mocks__/ACLRecipientModule.mock";
import {
  ACLExpression,
  compactACLExpressions,
  generate,
  generateHumanReadable,
  getACLExpressionById,
  parse,
  removeACLExpression,
  removeRedundantExpressions,
  revertCompactedACLExpressions,
} from "../../../tsrc/modules/ACLExpressionModule";
import { findUserById } from "../../../__mocks__/UserModule.mock";
import { findGroupById } from "../../../__mocks__/GroupModule.mock";
import { findRoleById } from "../../../__mocks__/RoleModule.mock";
import {
  aclEveryone,
  aclEveryoneInfix,
  aclFourItems,
  aclFourItemsInfix,
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
  emptyRecipientWithOneChildExpression,
  everyoneACLExpression,
  fourItemsACLExpression,
  nestedEmptyRecipientWithOneChildExpression,
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
  simplifiedEmptyRecipientWithOneChildExpression,
  simplifiedNestedEmptyRecipientWithOneChildExpression,
  simplifiedPeerSameOperatorExpression,
  threeItemsACLExpression,
  twoItemsACLExpression,
  userACLExpression,
  withMultipleSubExpression,
  withNestedSubExpressionACLExpression,
  withSubExpressionACLExpression,
} from "../../../__mocks__/ACLExpressionModule.mock";
import { ignoreId } from "./ACLExpressionModuleTestHelper";

describe("ACLExpressionModule", () => {
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

  const expectedRightResult = (
    aclExpression: ACLExpression
  ): E.Either<never, ACLExpression> => pipe(aclExpression, ignoreId, E.right);

  describe("parse", () => {
    it.each([
      ["everyone", aclEveryone, everyoneACLExpression],
      ["aclOwner", aclOwner, ownerACLExpression],
      ["aclUser", aclUser, userACLExpression],
      ["aclNotUser", aclNotUser, notUserACLExpression],
      ["aclTwoItems", aclTwoItems, twoItemsACLExpression],
      ["aclThreeItems", aclThreeItems, threeItemsACLExpression],
      ["fourItemsACLExpression", aclFourItems, fourItemsACLExpression],
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
    ])(
      "parse %s as ACL Expressions",
      (_, aclExpression, expectedExpression) => {
        expect(parse(aclExpression)).toEqual(
          expectedRightResult(expectedExpression)
        );
      }
    );

    it("text with a blank space at the end should still be able to parsed", () => {
      expect(parse(aclWithComplexSubExpression + " ")).toEqual(
        expectedRightResult(complexExpressionACLExpression)
      );
    });
  });

  describe("compactACLExpressions", () => {
    it("lift recipient and children into `NOT` ACLExpression", () => {
      expect(compactACLExpressions(notNestedExpression)).toEqual(
        ignoreId(notNestedCompactedExpression)
      );
      expect(compactACLExpressions(notExpression)).toEqual(
        ignoreId(notExpression)
      );
      expect(compactACLExpressions(notWithChildExpression)).toEqual(
        ignoreId(notWithChildCompactedExpression)
      );
      expect(compactACLExpressions(notUnexpectedExpression)).toEqual(
        ignoreId(notUnexpectedCompactedExpression)
      );
    });
  });

  describe("revertCompactedACLExpressions", () => {
    it("wrap recipient and children into an `OR` ACLExpression", () => {
      expect(revertCompactedACLExpressions(notExpression)).toEqual(
        ignoreId(notExpression)
      );
      expect(
        revertCompactedACLExpressions(notWithChildCompactedExpression)
      ).toEqual(ignoreId(notWithChildExpression));
      expect(
        revertCompactedACLExpressions(notNestedCompactedExpression)
      ).toEqual(ignoreId(notNestedExpression));
      expect(
        revertCompactedACLExpressions(notUnexpectedCompactedExpression)
      ).toEqual(ignoreId(notUnexpectedRevertCompactExpression));
    });

    it("multiple revert actions should still have same results", () => {
      expect(
        pipe(
          notExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(ignoreId(notExpression));
      expect(
        pipe(
          notWithChildCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(ignoreId(notWithChildExpression));
      expect(
        pipe(
          notNestedCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(ignoreId(notNestedExpression));
      expect(
        pipe(
          notUnexpectedCompactedExpression,
          revertCompactedACLExpressions,
          revertCompactedACLExpressions
        )
      ).toEqual(ignoreId(notUnexpectedRevertCompactExpression));
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
      ["fourItemsACLExpression", fourItemsACLExpression, aclFourItems],
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
      ["fourItemsACLExpression", fourItemsACLExpression, aclFourItemsInfix],
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
    it.each([
      [
        "childSameOperatorExpression",
        childSameOperatorExpression,
        simplifiedChildSameOperatorExpression,
      ],
      [
        "childOneItemRedundantExpression",
        childOneItemRedundantExpression,
        simplifiedChildOneItemRedundantExpression,
      ],
      [
        "childrenItemRedundantExpression",
        childrenItemRedundantExpression,
        simplifiedChildrenItemRedundantExpression,
      ],
      [
        "peerSameOperatorExpression",
        peerSameOperatorExpression,
        simplifiedPeerSameOperatorExpression,
      ],
      [
        "complexRedundantExpression",
        complexRedundantExpression,
        simplifiedComplexRedundantExpression,
      ],
      [
        "emptyRecipientWithOneChildExpression",
        emptyRecipientWithOneChildExpression,
        simplifiedEmptyRecipientWithOneChildExpression,
      ],
      [
        "nestedEmptyRecipientWithOneChildExpression",
        nestedEmptyRecipientWithOneChildExpression,
        simplifiedNestedEmptyRecipientWithOneChildExpression,
      ],
    ])(
      "remove redundant expressions in objects of ACL Expression",
      (_, rawACLExpression, simplifiedACLExpression) => {
        expect(removeRedundantExpressions(rawACLExpression)).toEqual(
          ignoreId(simplifiedACLExpression)
        );
      }
    );
  });

  describe("getACLExpressionById", () => {
    const child1: ACLExpression = {
      id: "test",
      operator: "OR",
      recipients: [user100Recipient],
      children: [],
    };

    const child2: ACLExpression = {
      id: "test",
      operator: "AND",
      recipients: [user200Recipient],
      children: [],
    };

    const aclExpression: ACLExpression = {
      id: "root",
      operator: "AND",
      recipients: [],
      children: [child1],
    };

    it("should return ACLExpression if it exists", () => {
      expect(pipe(aclExpression, getACLExpressionById("test"))).toEqual(child1);
    });

    it("should return undefined if it not exists", () => {
      expect(
        pipe(aclExpression, getACLExpressionById("non-exist"))
      ).toBeUndefined();
    });

    it("should log warning message and return undefined if it find more than one ACLExpression with same id", () => {
      const newACLExpression: ACLExpression = {
        ...aclExpression,
        children: [child1, child2],
      };
      const logWarning = jest.fn();
      global.console.warn = logWarning;

      const find = pipe(newACLExpression, getACLExpressionById("test"));

      expect(find).toBeUndefined();
      expect(logWarning).toHaveBeenCalled();
    });
  });

  describe("removeACLExpression", () => {
    const NODE_ID_ROOT = "root";
    const NODE_ID_TEST = "test";

    it("should be able to remove an ACLExpression by a given id", () => {
      const expectedResult = {
        ...initialACLExpression,
        children: [],
      };

      expect(
        pipe(initialACLExpression, removeACLExpression(NODE_ID_TEST))
      ).toEqual(O.some(expectedResult));
    });

    it("should return Option none if the given id represents the root ACLExpression", () => {
      expect(
        pipe(initialACLExpression, removeACLExpression(NODE_ID_ROOT))
      ).toEqual(O.none);
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
      ["fourItemsACLExpression", aclFourItems, fourItemsACLExpression],
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
        expectedRightResult(expectedACLExpression)
      );
    });

    it.each([
      ["everyone", everyoneACLExpression, aclEveryone],
      ["aclOwner", ownerACLExpression, aclOwner],
      ["aclUser", userACLExpression, aclUser],
      ["aclNotUser", notUserACLExpression, aclNotUser],
      ["aclTwoItems", twoItemsACLExpression, aclTwoItems],
      ["aclThreeItems", threeItemsACLExpression, aclThreeItems],
      ["fourItemsACLExpression", fourItemsACLExpression, aclFourItems],
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
