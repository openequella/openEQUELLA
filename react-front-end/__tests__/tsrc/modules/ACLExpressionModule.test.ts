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
  parse,
  removeRedundantExpressions,
  revertCompactedACLExpressions,
} from "../../../tsrc/modules/ACLExpressionModule";
import {
  aclEveryone,
  aclNotUser,
  aclOwner,
  aclThreeItems,
  aclTwoItems,
  aclUser,
  aclWithComplexSubExpression,
  aclWithMultipleSubExpression,
  aclWithNestedSubExpression,
  aclWithSubExpression,
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
    it("generate postfix ACL Expression text from objects of ACL Expression", () => {
      expect(generate(everyoneACLExpression)).toEqual(aclEveryone);
      expect(generate(ownerACLExpression)).toEqual(aclOwner);
      expect(generate(userACLExpression)).toEqual(aclUser);
      expect(generate(notUserACLExpression)).toEqual(aclNotUser);
      expect(generate(twoItemsACLExpression)).toEqual(aclTwoItems);
      expect(generate(threeItemsACLExpression)).toEqual(aclThreeItems);
      expect(generate(withSubExpressionACLExpression)).toEqual(
        aclWithSubExpression
      );
      expect(generate(withMultipleSubExpression)).toEqual(
        aclWithMultipleSubExpression
      );
      expect(generate(withNestedSubExpressionACLExpression)).toEqual(
        aclWithNestedSubExpression
      );
      expect(generate(complexExpressionACLExpression)).toEqual(
        aclWithComplexSubExpression
      );
    });
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
    it("parse -> generate -> parse", () => {
      expect(pipe(aclEveryone, parse, handleParse, generate, parse)).toEqual(
        E.right(everyoneACLExpression)
      );
      expect(pipe(aclOwner, parse, handleParse, generate, parse)).toEqual(
        E.right(ownerACLExpression)
      );
      expect(pipe(aclUser, parse, handleParse, generate, parse)).toEqual(
        E.right(userACLExpression)
      );
      expect(pipe(aclNotUser, parse, handleParse, generate, parse)).toEqual(
        E.right(notUserACLExpression)
      );
      expect(pipe(aclTwoItems, parse, handleParse, generate, parse)).toEqual(
        E.right(twoItemsACLExpression)
      );
      expect(pipe(aclThreeItems, parse, handleParse, generate, parse)).toEqual(
        E.right(threeItemsACLExpression)
      );
      expect(
        pipe(aclWithSubExpression, parse, handleParse, generate, parse)
      ).toEqual(E.right(withSubExpressionACLExpression));
      expect(
        pipe(aclWithMultipleSubExpression, parse, handleParse, generate, parse)
      ).toEqual(E.right(withMultipleSubExpression));
      expect(
        pipe(aclWithNestedSubExpression, parse, handleParse, generate, parse)
      ).toEqual(E.right(withNestedSubExpressionACLExpression));
      expect(
        pipe(aclWithComplexSubExpression, parse, handleParse, generate, parse)
      ).toEqual(E.right(complexExpressionACLExpression));
    });

    it("generate -> parse -> generate", () => {
      expect(
        pipe(everyoneACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclEveryone);
      expect(
        pipe(ownerACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclOwner);
      expect(
        pipe(userACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclUser);
      expect(
        pipe(notUserACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclNotUser);
      expect(
        pipe(twoItemsACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclTwoItems);
      expect(
        pipe(threeItemsACLExpression, generate, parse, handleParse, generate)
      ).toEqual(aclThreeItems);
      expect(
        pipe(
          withSubExpressionACLExpression,
          generate,
          parse,
          handleParse,
          generate
        )
      ).toEqual(aclWithSubExpression);
      expect(
        pipe(withMultipleSubExpression, generate, parse, handleParse, generate)
      ).toEqual(aclWithMultipleSubExpression);
      expect(
        pipe(
          withNestedSubExpressionACLExpression,
          generate,
          parse,
          handleParse,
          generate
        )
      ).toEqual(aclWithNestedSubExpression);
      expect(
        pipe(
          complexExpressionACLExpression,
          generate,
          parse,
          handleParse,
          generate
        )
      ).toEqual(aclWithComplexSubExpression);
    });
  });
});
