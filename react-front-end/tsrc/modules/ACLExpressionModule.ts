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
import * as t from "io-ts";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { constant, flow, identity, pipe } from "fp-ts/function";
import { Monoid } from "fp-ts/Monoid";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as RNEA from "fp-ts/ReadonlyNonEmptyArray";
import { ReadonlyNonEmptyArray } from "fp-ts/ReadonlyNonEmptyArray";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import { v4 as uuidv4 } from "uuid";
import { languageStrings } from "../util/langstrings";
import { simpleUnionMatch } from "../util/match";
import { pfTernary, pfTernaryTypeGuard } from "../util/pointfree";
import { ACLEntityResolversMulti, ACLEntityResolvers } from "./ACLEntityModule";
import {
  ACLRecipient,
  createACLRecipient,
  showRecipient,
  showRecipientHumanReadable,
} from "./ACLRecipientModule";
import { findGroupById, findGroupsByIds } from "./GroupModule";
import { findRoleById, findRolesByIds } from "./RoleModule";
import { findUserById, resolveUsers } from "./UserModule";

const {
  aclExpressionBuilder: {
    operators: { or: orLabel, and: andLabel, not: notLabel },
  },
} = languageStrings;

/**
 * ACL Operator types:
 *
 * `UNKNOWN` is only used as a placeholder while parsing an ACL recipient string.
 */
export const ACLOperatorTypesUnion = t.union([
  t.literal("OR"),
  t.literal("AND"),
  t.literal("NOT"),
  t.literal("UNKNOWN"),
]);

export type ACLOperatorType = t.TypeOf<typeof ACLOperatorTypesUnion>;

export const getOperatorLabel = simpleUnionMatch<ACLOperatorType, string>({
  OR: () => orLabel,
  AND: () => andLabel,
  NOT: () => notLabel,
  UNKNOWN: () => "UNKNOWN",
});

/**
 * Represents the ACL Expression string.
 *
 * A raw ACL Expression string can be parsed into an ACLExpression object.
 * And an ACLExpression object can also generate a raw ACL Expression string.
 */
export interface ACLExpression {
  /**
   * A unique identifier used to render a tree view and locate the selected ACLExpression.
   */
  id: string;
  /**
   * Operator relationship between recipient and children.
   */
  operator: ACLOperatorType;
  /**
   * A list of target that will be authorised or unauthorised.
   */
  recipients: ACLRecipient[];
  /**
   * A list of sub ACL expression.
   */
  children: ACLExpression[];
}

/**
 * Generate ACLExpression object to a readable string format, mainly used for debug.
 */
export const generateACLExpressionDisplayString = (
  { operator, children, recipients }: ACLExpression,
  padding = "",
): string =>
  pipe(
    [padding, operator],
    A.concat(recipients.map((r) => showRecipient(r))),
    A.intercalate(S.Monoid)(" "),
    A.of,
    A.concat(
      pipe(
        children,
        A.map((c) => generateACLExpressionDisplayString(c, padding + " ")),
      ),
    ),
    A.intercalate(S.Monoid)("\n"),
  );

/**
 * Create a new object of ACLExpression through given params.
 */
export const createACLExpression = (
  operator: ACLOperatorType,
  recipients: ACLRecipient[] = [],
  children: ACLExpression[] = [],
): ACLExpression => ({
  id: uuidv4(),
  operator: operator,
  recipients: [...recipients],
  children: [...children],
});

/**
 * Creates a new `ACLExpression` based on the provided `expression` with the additional `recipients` added.
 */
export const addRecipients =
  (recipients: ACLRecipient[]) =>
  (expression: ACLExpression): ACLExpression => ({
    ...expression,
    recipients: pipe(expression.recipients, A.concat(recipients)),
  });

/**
 * Creates a new `ACLExpression` based on the provided `expression` with the additional `children` added.
 */
const addChildren =
  (child: ACLExpression[]) =>
  (expression: ACLExpression): ACLExpression => ({
    ...expression,
    children: pipe(expression.children, A.concat(child)),
  });

/**
 * Find the original ACLExpression by id in `originalACLExpression` and replace it with `newACLExpression`.
 * If it can't find the corresponding ACLExpression in `originalACLExpression` it will return a new `originalACLExpression`
 * (recursively) .
 *
 * All returned result will be a new object.
 */
export const replaceACLExpression = (
  newACLExpression: ACLExpression,
): ((originalACLExpression: ACLExpression) => ACLExpression) =>
  pfTernary(
    (originalACLExpression: ACLExpression) =>
      originalACLExpression.id === newACLExpression.id,
    (_) => newACLExpression,
    (originalACLExpression: ACLExpression) =>
      pipe(
        originalACLExpression.children,
        A.map(replaceACLExpression(newACLExpression)),
        (newChildren) => ({
          ...originalACLExpression,
          children: newChildren,
        }),
      ),
  );

/**
 * Find the `aclExpression` by id in `originalACLExpression` and remove it (recursively).
 */
export const removeACLExpression = (
  id: string,
): ((originalACLExpression: ACLExpression) => O.Option<ACLExpression>) =>
  pfTernary(
    (aclExpression: ACLExpression) => aclExpression.id === id,
    constant(O.none),
    (originalACLExpression: ACLExpression) =>
      pipe(
        originalACLExpression.children,
        A.filterMap(removeACLExpression(id)),
        (newChildren) =>
          O.some({
            ...originalACLExpression,
            children: newChildren,
          }),
      ),
  );

/**
 * Given an ACLExpression and an ID, recursively search for an ACLExpression that has the same ID from
 * the given ACLExpression and its nested ACLExpressions.
 *
 * It will return `undefined` if multiple ACLExpression are found.
 */
export const getACLExpressionById = (
  id: string,
): ((aclExpression: ACLExpression) => ACLExpression | undefined) =>
  pfTernary<ACLExpression, ACLExpression | undefined>(
    (aclExpression) => aclExpression.id === id,
    identity,
    ({ children }) =>
      pipe(
        children,
        A.filterMap(flow(getACLExpressionById(id), O.fromNullable)),
        NEA.fromArray,
        O.chainFirstEitherK(
          E.fromPredicate(
            (a) => A.size(a) <= 1,
            (error) =>
              console.warn(`Find more than one ACLExpression with ID ${id}`),
          ),
        ),
        O.map(NEA.head),
        O.toUndefined,
      ),
  );

/**
 * Return a collection containing all recipients in a given ACLExpression (recursively).
 */
export const flattenRecipients = ({
  children,
  recipients,
}: ACLExpression): ACLRecipient[] =>
  pipe(children, A.chain(flattenRecipients), A.concat(recipients));

/**
 * Return a collection containing all ACLExpression(root + children) IDs in a given ACLExpression (recursively).
 */
export const flattenIds = ({ id, children }: ACLExpression): string[] =>
  pipe(children, A.chain(flattenIds), A.concat([id]));

/**
 * Get the number of recipients for a given ACLExpression.
 */
const getRecipientCount = (expression: ACLExpression): number =>
  pipe(expression.recipients, A.size);

/**
 * Get the number of children for a given ACLExpression.
 */
const getChildrenCount = (expression: ACLExpression): number =>
  pipe(expression.children, A.size);

/**
 * Tells whether the provide piece of text is an ACL Operator.
 */
const isACLOperator = (text: string): text is ACLOperatorType =>
  ACLOperatorTypesUnion.is(text);

/**
 * Merge a given base ACL Expression with a new ACL Expression.
 *
 * If the new ACLExpression's operator is `UNKNOWN` or same with the base ACLExpression,
 * then generate a new ACLExpression with both recipients and children from the two ACLExpressions.
 *
 * Otherwise, generate a new ACLExpression which regard the new ACLExpression as a child of the basement ACLExpression.
 *
 * Example 1:
 * ```
 * {
 *   operator: "AND",
 *   recipients: [A, B],
 *   children: [child1]
 * } merge with
 * {
 *   operator: "AND",
 *   recipients: [C],
 *   children: [child2]
 * } =
 * {
 *   operator: "AND",
 *   recipients: [A, B, C],
 *   children: [child1, child2]
 * }
 * ```
 *
 * Example 2:
 * ```
 * {
 *   operator: "AND",
 *   recipients: [A, B],
 *   children: [child1]
 * } merge with
 * {
 *   operator: "OR",
 *   recipients: [C],
 *   children: [child2]
 * } =
 * {
 *   operator: "AND",
 *   recipients: [A, B],
 *   children: [
 *     child1,
 *     {
 *       operator: "OR",
 *       recipients: [C],
 *       children: [child2]
 *     }
 *   ]
 * }
 * ```
 */
const merge = (
  baseExpression: ACLExpression,
  newExpression: ACLExpression,
): ACLExpression => {
  return baseExpression.operator === newExpression.operator ||
    newExpression.operator === "UNKNOWN"
    ? pipe(
        baseExpression,
        addChildren(newExpression.children),
        addRecipients(newExpression.recipients),
      )
    : pipe(baseExpression, addChildren([newExpression]));
};

/**
 * Build an ACLExpression from the given text.
 *
 * Example 1:
 *
 * Text:
 * ```
 * "U:USER1"
 * ```
 * Result:
 * ```
 * {
 *   operator: "UNKNOWN",
 *   recipient: [ USER1 ],
 *   children: []
 * }
 *```
 *
 * Example 2:
 *
 * Text:
 * ```
 * "AND"
 * ```
 * Result:
 * ```
 * {
 *   operator: "AND",
 *   recipient: [],
 *   children: []
 * }
 * ```
 */
const buildACLExpression = (text: string): E.Either<string, ACLExpression> =>
  pipe(
    text,
    pfTernaryTypeGuard(
      isACLOperator,
      flow(createACLExpression, E.right),
      flow(
        createACLRecipient,
        E.map((recipient) => createACLExpression("UNKNOWN", [recipient])),
      ),
    ),
  );

/**
 * Tells how many operands (`ACLExpression`s) the given operator supports. That is, NOT can only work with
 * one operand, however AND/OR are binary operators and can work on two operands.
 */
const howManyOperands = (operator: ACLOperatorType) =>
  operator === "NOT" ? 1 : 2;

/**
 * Used to track the Parser's state while processing an ACL Expression string.
 */
interface AclExpressionBuildingState {
  /** The holder for the final result as it is being constructed. */
  result: ACLExpression;
  /**
   * A LIFO stack of the immediate previous expressions which are merged with subsequent expressions
   * as the parsing progresses.
   */
  previousExpressions: ACLExpression[];
}

/**
 * Parse a raw ACL expression string and return an object of ACLExpression.
 *
 * First it will get a list of raw expressions from string and wrap them into ACLExpression objects.
 *
 * Then it will then traverse all objects, and generate the result we want.
 *
 * Details of traverse (function parseACLExpressions):
 *
 * If there is only one `ACLExpression` in the list direct return it with `OR` operator.
 *
 * Traverse all ``ACLExpression`` in the list, if the text belongs to recipient before wrap, put it into the pending list(previousExpressions).
 * If it belongs to the operator before wrap, then extract 1 or 2 ACLExpression object from the pending list according to the type of operator.
 * The ACLExpressions extracted from pending list, will be merged into current ACLExpression and become a new result.
 * It will also generate a new pending list(previousExpressions) which excludes the expressions we already merged but includes the new result which will wait for the next iteration.
 *
 * Example 1:
 * expression: A B AND C OR
 * ```
 * iteration 1:
 * {
 *   result: ()
 *   previousExpressions: [A]
 * }
 * iteration 2:
 * {
 *   result: ()
 *   previousExpressions: [A, B]
 * }
 * iteration 3:
 * {
 *   result: (A B AND)
 *   previousExpressions: [(A B AND)]
 * }
 * iteration 4:
 * {
 *   result: (A B AND)
 *   previousExpressions: [(A B AND), C]
 * }
 * iteration 5:
 * {
 *   result: (A B AND C OR)
 *   previousExpressions: [(A B AND C OR)]
 * }
 * ```
 *
 * Example 2:
 * expression: A B C OR NOT AND
 * ```
 * iteration 1:
 * {
 *   result: ()
 *   previousExpressions: [A]
 * }
 * iteration 2:
 * {
 *   result: ()
 *   previousExpressions: [A, B]
 * }
 * iteration 3:
 * {
 *   result: ()
 *   previousExpressions: [A, B, C]
 * }
 * iteration 4:
 * {
 *   result: (B C OR)
 *   previousExpressions: [A, (B C OR)]
 * }
 * iteration 5:
 * {
 *   result: (B C OR NOT)
 *   previousExpressions: [A, (B C OR NOT)]
 * }
 * iteration 6:
 * {
 *   result: (B C OR NOT AND)
 *   previousExpressions: [(A B C OR NOT AND)]
 * }
 * ```
 *
 * @param expression Raw ACL expression string.
 */
export const parse = (
  expression: string,
): E.Either<string[], ACLExpression> => {
  /**
   * Assumes `currentExpression` has an operator other than `UNKNOWN` then merges the `previousExpressions` from state into the `currentExpression`.
   * It then returns a new state object where the result is updated to the output of the merging,
   * and the `previousExpressions` are updated to drop those which have been used with result appended to the end.
   */
  const handleKnownExpression = (
    { result, previousExpressions }: AclExpressionBuildingState,
    currentExpression: ACLExpression,
  ): AclExpressionBuildingState =>
    pipe(
      previousExpressions,
      A.takeRight(howManyOperands(currentExpression.operator)),
      A.reduce<ACLExpression, ACLExpression>(
        currentExpression,
        (mergedExpressions, nextExpression) =>
          merge(mergedExpressions, nextExpression),
      ),
      (fullExpression) => ({
        result: fullExpression,
        previousExpressions: pipe(
          previousExpressions,
          A.dropRight(howManyOperands(currentExpression.operator)),
          A.append(fullExpression),
        ),
      }),
    );

  /**
   * Assumes `currentExpression` has an operator of `UNKNOWN` and appends it to the existing `previousExpressions` before returning the updated state.
   */
  const handleUnknownExpression = (
    { result, previousExpressions }: AclExpressionBuildingState,
    currentExpression: ACLExpression,
  ) => ({
    result,
    previousExpressions: [...previousExpressions, currentExpression],
  });

  /**
   * Traverse a give aclExpression list, merge them one by one and generate a final ACLExpression object.
   */
  const parseACLExpressions = (
    aclExpressions: ReadonlyNonEmptyArray<ACLExpression>,
  ): ACLExpression =>
    pipe(
      aclExpressions,
      O.fromPredicate((a) => RA.size(a) > 1),
      O.match(
        () =>
          // If there is only one 'ACLExpression' then that means it should be a recipient,
          // and we simply wrap that in an 'OR' and return it.
          createACLExpression(
            "OR",
            pipe(
              aclExpressions,
              RNEA.head,
              (expression) => expression.recipients,
            ),
          ),
        (expressions) =>
          pipe(
            expressions,
            RA.reduce<ACLExpression, AclExpressionBuildingState>(
              {
                result: {
                  id: uuidv4(),
                  operator: "UNKNOWN",
                  recipients: [],
                  children: [],
                },
                previousExpressions: [],
              },
              (currentState, currentExpression) => {
                const handler =
                  currentExpression.operator !== "UNKNOWN"
                    ? handleKnownExpression
                    : handleUnknownExpression;
                return handler(currentState, currentExpression);
              },
            ),
            ({ result }) => result,
          ),
      ),
    );

  return pipe(
    expression,
    S.split(" "),
    RA.filter(not(S.isEmpty)),
    RA.map(buildACLExpression),
    E.fromPredicate(not(RA.some(E.isLeft)), (errors) =>
      pipe(errors, RA.lefts, RA.toArray),
    ),
    E.chain((expressions) =>
      pipe(
        expressions,
        RA.rights,
        RNEA.fromReadonlyArray,
        O.map(parseACLExpressions),
        E.fromOption(() => ["Received empty ACLExpression list"]),
      ),
    ),
  );
};

/**
 * Prepare a simpler tree structure from `parse` for rendering in the UI.
 * This only has to do with the handling of None groupings (or syntactically, NOT operators).
 *
 * Note:
 * For a normal tree structure used to generate an expression string,
 * `NOT` is a very special Operator that in theory if an ACLExpression's operator is `NOT` it can only have one recipient or one `OR` child.
 *
 * Case 1:
 * ```
 * NOT
 *   A
 * ```
 *
 * Case 2:
 * ```
 * NOT
 *   OR
 *     A B
 * ```
 *
 * But it's a bit cumbersome for users, in order to simply the UI we have another type of ACLExpression,
 * called `compacted ACLExpression`
 * which will compact a `NOT` ACLExpression make it act like a normal ACLExpression
 * that can have multiple recipients and children.
 *
 * Implementation Details:
 * For a `NOT` ACLExpression, the function extracts all children's recipients and their children, and
 * then add all recipients to its recipients attribute and overwrite its children attribute with new children. (recursively).
 *
 * Besides, although in theory the input `NOT` ACLExpression should follow the rules mention above(only one recipient or child),
 * if the received ACLExpression breaks rules it will still compact the ACLExpression.
 * (Thus, although there is another function can be used to revert the compacted ACLExpression
 * , in this case you will get a different ACLExpression from what you received here.)
 *
 * Example 1:
 *
 * input:
 * ```
 * NOT
 *   A
 * ```
 *
 * output:
 * ```
 * NOT
 *   A
 * ```
 *
 * Example 2:
 *
 * input:
 * ```
 * NOT
 *   OR
 *     A B
 *     AND
 *       C D
 * ```
 *
 * output:
 * ```
 * NOT
 *   A B
 *   AND
 *     C D
 * ```
 *
 * Example 3:
 *
 * unexpected input:
 * ```
 * NOT
 *   A B
 *   OR
 *     C D
 *     AND
 *       E F
 *   AND
 *     G H
 * ```
 *
 * output:
 * ```
 * NOT
 *   A B C D G H
 *   AND
 *     E F
 * ```
 * */
export const compactACLExpressions = (
  aclExpression: ACLExpression,
): ACLExpression =>
  pipe(
    aclExpression.children,
    A.map(compactACLExpressions),
    pfTernary(
      () => aclExpression.operator === "NOT",
      A.reduce<ACLExpression, ACLExpression>(
        createACLExpression("NOT", aclExpression.recipients),
        (result, { recipients, children }) =>
          pipe(result, addRecipients(recipients), addChildren(children)),
      ),
      (newChildren) => ({
        ...aclExpression,
        children: newChildren,
      }),
    ),
  );

/**
 * This function is used to proceed `NOT` ACLExpression in function `revertCompactedACLExpressions`.
 *
 * For ACLExpression which only have one recipient, it will return itself.
 * Otherwise, it will move all recipients and children to a new `OR` ACLExpression,
 * then return a new `NOT` ACLExpression with the new `OR` child.
 * Finally, it will remove all redundant expressions.
 */
const handleNotACLExpression = (
  aclExpression: ACLExpression,
  revertedChildren: ACLExpression[],
) =>
  A.isEmpty(revertedChildren) && getRecipientCount(aclExpression) === 1
    ? aclExpression
    : pipe(
        createACLExpression("OR", aclExpression.recipients, revertedChildren),
        (result) => createACLExpression("NOT", [], [result]),
        removeRedundantExpressions,
      );

/**
 * Prepare the tree structure users build with the UI for being sent to generate.
 * Convert the compacted tree structure and make the result can be passed to function `generate`.
 *
 * Implementation Details (recursively):
 * For `NOT` ACLExpression
 * Step 1: unless it only have one recipient it will move all recipients and children to a new `OR` ACLExpression.
 * Step 2: return a new `NOT` ACLExpression with the new `OR` child which we get from step 1.
 * Step 3: removing all redundant expressions.
 *
 * Besides, if you revert an expression multiple times the final output won't be changed.
 * (ie. expressionA  -> revert -> expressionB -> revert -> expressionB -> revert -> expressionB )
 *
 * Example 1:
 *
 * input:
 * ```
 * NOT
 *   A
 * ```
 *
 * output:
 * ```
 * NOT
 *   A
 * ```
 *
 *
 * Example 2:
 *
 * input:
 * ```
 * NOT
 *   OR
 *     A B
 * ```
 *
 * output:
 * ```
 * NOT
 *   OR
 *     A B
 * ```
 *
 * Example 3:
 *
 * input:
 * ```
 * NOT
 *   A B
 *   AND
 *     C D
 * ```
 *
 * output:
 * ```
 * NOT
 *   OR
 *     A B
 *     AND
 *       C D
 * ```
 *
 * Example 4:
 *
 * input:
 * ```
 * NOT
 *   A B C D G H
 *   AND
 *     E F
 * ```
 *
 * output:
 * ```
 *   NOT
 *     OR
 *       A B C D G H
 *       AND
 *         E F
 * ```
 * */
export const revertCompactedACLExpressions = (
  aclExpression: ACLExpression,
): ACLExpression =>
  pipe(
    aclExpression.children,
    A.map(revertCompactedACLExpressions),
    (revertedChildren: ACLExpression[]) =>
      aclExpression.operator === "NOT"
        ? handleNotACLExpression(aclExpression, revertedChildren)
        : {
            ...aclExpression,
            children: revertedChildren,
          },
  );

/**
 * Return a new AclExpression without any empty children (recursively).
 * Empty ACLExpression means it doesn't have any children and recipients.
 *
 * Example:
 *
 * input:
 * ```
 * AND A B C
 *   OR
 *   OR D E
 *      AND F G
 *      AND
 * ```
 * output:
 * ```
 * AND A B C
 *   OR D E
 *     AND F G
 * ```
 */
const filterEmptyChildren = (expression: ACLExpression): ACLExpression =>
  pipe(
    expression.children,
    A.filter(
      ({ children, recipients }) =>
        A.isNonEmpty(children) || A.isNonEmpty(recipients),
    ),
    A.map(filterEmptyChildren),
    (results) =>
      createACLExpression(
        expression.operator,
        [...expression.recipients],
        [...results],
      ),
  );

/** Does this expression have _only_ a single recipient and no children? */
const singleRecipientOnlyExpression = (e: ACLExpression): boolean =>
  getRecipientCount(e) === 1 && A.isEmpty(e.children);

/** Does this expression have _only_ one child and no recipient? */
const singleChildOnlyExpression = (e: ACLExpression): boolean =>
  getChildrenCount(e) === 1 && A.isEmpty(e.recipients);

/**
 * Return a new AclExpression without any children which has same operator with itself (recursively).
 * It performs merge operations on every child,
 * using the parent's operator and recipients as the base.
 *
 * If a child has same operator with parent or a child only has one recipient,
 * merge this child's recipients and children into parents.
 *
 * And if parent has no recipient and with only one child, then only keep the child.
 *
 * Besides, the above 2 rules will not apply to a child if it's operator is "NOT".
 *
 * Example 1:
 *
 * input:
 * ```
 * OR A B C
 *   OR D E
 * ```
 * output:
 * ```
 * OR A B C D E
 * ```
 *
 * Example 2:
 *
 * input:
 * ```
 * OR A B C
 *   AND D
 * ```
 * output:
 * ```
 * OR A B C D
 * ```
 *
 * Example 3:
 *
 * input:
 * ```
 * OR
 *   AND A B
 * ```
 * output:
 * ```
 * AND A B
 * ```
 */
const mergeChildren = (expression: ACLExpression): ACLExpression => {
  const mergeChildrenMonoid: Monoid<ACLExpression> = {
    empty: createACLExpression(expression.operator, [...expression.recipients]),
    concat: (x: ACLExpression, y: ACLExpression): ACLExpression => {
      const handleExpressionWithRecipient = (child: ACLExpression) =>
        child.operator !== "NOT" && singleRecipientOnlyExpression(child)
          ? addRecipients(child.recipients)(x)
          : merge(x, child);

      return expression.operator !== "NOT" &&
        singleChildOnlyExpression(expression)
        ? y
        : handleExpressionWithRecipient(y);
    },
  };

  return pipe(
    expression.children,
    A.foldMap(mergeChildrenMonoid)(mergeChildren),
  );
};

/**
 * Remove redundant expressions, and return a new ACLExpression object.
 */
export const removeRedundantExpressions = (aclExpression: ACLExpression) =>
  pipe(aclExpression, filterEmptyChildren, mergeChildren);

/**
 * Generate postfix Acl expression strings, and store them in an array, and later they can be combined as a single string easily.
 *
 * First if the ACLExpression only has one recipient and the operator is not `NOT` just return the expression itself.
 * If it has more than one recipient, for each two recipients it will insert an operator and then if the number of recipient is odd, insert another operator at the end.
 * Then start to process each child.
 * Finally, insert an operator after each child's result.
 *
 * Example 1:
 *
 * input:
 * ```
 * AND A B C
 * ```
 *
 * result:
 * ```
 *  [A]
 *  [A, B]
 *  [A, B, AND]
 *  [A, B, AND, C]
 *  [A, B, AND, C, AND]
 * ```
 * Example 2:
 *
 * input:
 * ```
 * AND A
 *   OR B C
 * ```
 *
 * result:
 * ```
 *  [A]
 *  [A, AND]
 *  [A, AND, B]
 *  [A, AND, B, C]
 *  [A, AND, B, C, OR]
 * ```
 */
const generatePostfixResults = (aclExpression: ACLExpression): string[] => {
  // For expressions with single recipients:
  // 1. If the expression has an operator of NOT then return an array of `[expression, NOT]`
  // 2. Otherwise, encapsulate the expression in a single element array. i.e. `[expression]`
  const handleSingleRecipient = (
    operator: ACLOperatorType,
    recipient: ACLRecipient,
  ): string[] =>
    operator === "NOT"
      ? [showRecipient(recipient), operator]
      : [showRecipient(recipient)];

  // This function helps to insert the operator between elements and flatten the array.
  const insertOperators =
    (operator: string) =>
    (results: string[][]): string[] =>
      pipe(
        results,
        A.reduceWithIndex<string[], string[]>([], (index, acc, childResult) =>
          // Insert an operator between each elements, except for the first element.
          index > 1
            ? [...acc, aclExpression.operator, ...childResult]
            : [...acc, ...childResult],
        ),
        // Make sure the operator is always added on the end
        pfTernary(A.isNonEmpty, A.append(operator), identity),
      );

  const handleRecipients = (
    operator: ACLOperatorType,
    recipients: ACLRecipient[],
  ): string[] =>
    pipe(
      recipients,
      A.map((r) => [showRecipient(r)]),
      insertOperators(operator),
    );

  // generate results from children and combine it with recipients result
  const handleWithChildren =
    (recipientResult: string[]) =>
    (children: ACLExpression[]): string[] =>
      pipe(
        children,
        A.map(generatePostfixResults),
        // if recipientResult is non-empty, prepend it
        (results) =>
          A.isNonEmpty(recipientResult)
            ? [recipientResult, ...results]
            : results,
        // Insert operators between results
        insertOperators(aclExpression.operator),
      );

  return pipe(
    // Process the recipients
    aclExpression,
    revertCompactedACLExpressions,
    ({ operator, recipients }: ACLExpression) =>
      A.isNonEmpty(recipients) && A.size(recipients) === 1
        ? handleSingleRecipient(operator, NEA.head(recipients))
        : handleRecipients(operator, recipients),
    // ... then process the children if it's not empty
    (recipientResult) =>
      pipe(
        aclExpression.children,
        O.fromPredicate(A.isNonEmpty),
        O.map(handleWithChildren(recipientResult)),
        O.getOrElse(() => recipientResult),
      ),
  );
};

/**
 * Generate infix ACL expression by placing each part of the expression (operands, operators, and parentheses)
 * in an element of an array of strings. This array can later be easily combined to form a complete string.
 *
 * First if the `ACLExpression` only has one recipient and is an operator other than `NOT`, then only return the expression name.
 * Except the first recipient, every time it will insert an operator before adding a new recipient.
 * Then start to process each child.
 * Finally, insert an operator after each child's result. If the child's result has more than one recipient, then
 * parentheses will be used to group the results.
 *
 * Example 1:
 *
 * input:
 * ```
 * AND A B C
 * ```
 *
 * result:
 * ```
 *  [A]
 *  [A, AND, B]
 *  [A, AND, B, AND, C]
 * ```
 * Example 2:
 *
 * input:
 * ```
 * AND A
 *   OR B C
 * ```
 *
 * result:
 * ```
 *  [A]
 *  [A] [B, OR, C]
 *  [A, AND, (, B, OR, C, )]
 * ```
 * */
const generateInfixResults =
  (providers: ACLEntityResolvers) =>
  (aclExpression: ACLExpression): T.Task<string[]> => {
    // fetch human-readable from server if it's failed log the error and return the raw expression
    const getHumanReadableResult = (recipient: ACLRecipient) =>
      pipe(
        recipient,
        showRecipientHumanReadable(providers),
        TE.match((err: string) => {
          console.error(err);
          return showRecipient(recipient);
        }, identity),
      );

    // For expressions with single recipients:
    // 1. If the expression has an operator of NOT then return an array of `[NOT, expression]`
    // 2. Otherwise, encapsulate the expression in a single element array. i.e. `[expression]`
    const handleSingleRecipient = (
      operator: ACLOperatorType,
      recipient: ACLRecipient,
    ): T.Task<string[]> =>
      pipe(
        recipient,
        getHumanReadableResult,
        T.map<string, string[]>((name) =>
          operator === "NOT" ? [operator, name] : [name],
        ),
      );

    const handleRecipients = (
      operator: ACLOperatorType,
      recipients: ACLRecipient[],
    ): T.Task<string[]> =>
      pipe(
        recipients,
        A.map(getHumanReadableResult),
        T.sequenceArray,
        T.map(flow(RA.intersperse<string>(operator), RA.toArray)),
      );

    const processRecipients = ({ operator, recipients }: ACLExpression) =>
      A.isNonEmpty(recipients) && A.size(recipients) === 1
        ? handleSingleRecipient(operator, NEA.head(recipients))
        : handleRecipients(operator, recipients);

    const processChildren =
      (parentOperator: string) =>
      (children: ACLExpression[]): T.Task<string[]> =>
        pipe(
          children,
          A.map(generateInfixResults(providers)),
          T.sequenceArray,
          T.map<readonly string[][], string[]>(
            flow(
              RA.intersperse<string[]>([")", parentOperator, "("]),
              RA.flatten,
              pfTernary(
                RA.isNonEmpty,
                (result) => ["(", ...result, ")"],
                RA.toArray,
              ),
            ),
          ),
        );

    /**
     * Insert an operator between recipient result and children result if they are not empty.
     * For `NOT` operator always add a `NOT` prefix, even if the recipient result is empty.
     *
     * ( It is due to the structure of `NOT` ACLExpression.
     *   For example, this is a raw (un-compacted) structure of ACLExpression `NOT` node with multiple recipients,
     *   and all those recipients will be stored in a child `OR` node:
     *   ```
     *   NOT
     *     OR A B
     *   ```
     *   In this case, the `childrenResult` is ["(", "A", "OR", "B", ")"] while `recipientsResult` is an empty array.
     *   Thus, here the operator `NOT` will be added.
     *   But for `NOT` ACLExpression with only one recipient, the structure is different:
     *   ```
     *    NOT A
     *   ```
     *   In this case, `recipientsResult` would be  ["NOT", "A"], and no need to add the operator `NOT` here.
     * )
     */
    const combineRecipientAndChildrenResult = (
      recipientsResult: string[],
      childrenResult: string[],
    ) => {
      if (A.isEmpty(recipientsResult) && A.isEmpty(childrenResult)) {
        return [];
      }

      if (A.isEmpty(recipientsResult)) {
        return aclExpression.operator !== "NOT"
          ? childrenResult
          : [aclExpression.operator, ...childrenResult];
      }

      if (A.isEmpty(childrenResult)) {
        return recipientsResult;
      }

      return [...recipientsResult, aclExpression.operator, ...childrenResult];
    };

    return pipe(
      aclExpression,
      // Make sure it doesn't use the reverted form of ACLExpression. Because the following logic assume
      // `NOT` expression only have one child or one recipient.
      revertCompactedACLExpressions,
      processRecipients,
      // ... then process the children
      T.chain((recipientsResult) =>
        pipe(
          aclExpression.children,
          processChildren(aclExpression.operator),
          T.map((childrenResult: string[]) =>
            combineRecipientAndChildrenResult(recipientsResult, childrenResult),
          ),
        ),
      ),
    );
  };

/**
 * Generate infix Acl expression.
 * The infix format will be used for human to read.
 */
export const generateHumanReadable =
  (providers: ACLEntityResolvers) =>
  (aclExpression: ACLExpression): T.Task<string> =>
    pipe(
      aclExpression,
      generateInfixResults(providers),
      T.map(A.intercalate(S.Monoid)(" ")),
    );

/**
 * Generate postfix Acl expression.
 * The postfix format which will stored in DB. While the infix format will be used for human to read.
 *
 * Note: in old implementation all the expression (postfix format) stored in DB will end with a blank space except those "singular" expression.
 * Here don't have this end space since it's useless.
 *
 * Example of "singular" expression:
 * - *
 * - $owner
 * - U:user-id
 */
export const generate = (aclExpression: ACLExpression): string =>
  pipe(aclExpression, generatePostfixResults, A.intercalate(S.Monoid)(" "));

export const defaultACLEntityResolvers: ACLEntityResolvers = {
  resolveGroupProvider: findGroupById,
  resolveUserProvider: findUserById,
  resolveRoleProvider: findRoleById,
};

export const defaultACLEntityMultiResolvers: ACLEntityResolversMulti = {
  resolveGroupsProvider: findGroupsByIds,
  resolveUsersProvider: resolveUsers,
  resolveRolesProvider: findRolesByIds,
};
