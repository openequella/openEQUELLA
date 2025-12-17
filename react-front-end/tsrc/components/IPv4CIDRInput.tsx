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
import { Grid, TextField } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { constant, constFalse, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as S from "fp-ts/string";
import * as React from "react";
import { createRef, RefObject, useState, useRef } from "react";
import * as N from "fp-ts/number";
import * as NEA from "fp-ts/NonEmptyArray";
import * as RA from "fp-ts/ReadonlyArray";
import * as RNEA from "fp-ts/ReadonlyNonEmptyArray";
import { simpleUnionMatch } from "../util/match";

/**
 * Runtypes definition for key code which need to be handled here.
 */
const KeyCodeTypesUnion = t.union([
  t.literal("Enter"),
  t.literal("Backspace"),
  t.literal("Period"),
  t.literal("NumpadDecimal"),
]);

type KeyCodeTypes = t.TypeOf<typeof KeyCodeTypesUnion>;

const PREFIX = "IPv4CIDRInput";
const classes = {
  ipInput: `${PREFIX}-ipInput`,
  infix: `${PREFIX}-infix`,
};
const StyledGrid = styled(Grid)(({ theme }) => ({
  [`& .${classes.ipInput}`]: {
    width: "56px",
  },
  [`& .${classes.infix}`]: {
    padding: theme.spacing(0, 1),
    fontSize: "xx-large",
  },
}));

const ipElements = 4;
const defaultNetmask = 32;

export interface IPv4CIDRInputProps {
  /**
   * Initial value.
   * For example: "192.168.1.1/24".
   * Default value is empty string.
   */
  value?: string;
  /**
   * Trigger when all inputs have a valid value.
   *
   * @param address IPv4 CIDR Address
   * @param focus Index value represents the last focus `Input`
   */
  onChange: (address: string) => void;
}

/**
 * Contains 5 `Inputs` (4 inputs for ip address and 1 for the netmask).
 * It will automatically `focus` on next `Input` if users press `enter`, `.` or a valid value in each `Input`.
 * And the `focus` will automatically switch to previous `Input` by deleting value in current `Input`.
 */
const IPv4CIDRInput = ({ value = "", onChange }: IPv4CIDRInputProps) => {
  const separatedValue = pipe(value, S.split("/"));

  const [ipAddress, setIPAddress] = useState<
    RNEA.ReadonlyNonEmptyArray<string>
  >(
    pipe(
      separatedValue,
      RNEA.head,
      S.split("."),
      O.fromPredicate(
        (ip: RNEA.ReadonlyNonEmptyArray<string>) => ip.length === ipElements,
      ),
      O.getOrElse(() => pipe(ipElements, RNEA.replicate(""))),
    ),
  );
  const [netmask, setNetmask] = useState<string>(
    pipe(separatedValue, RNEA.last),
  );

  const ipInputRefs = useRef<
    NEA.NonEmptyArray<RefObject<HTMLInputElement | null>>
  >(
    pipe(
      NEA.range(1, ipElements),
      NEA.map((_) => createRef()),
    ),
  );
  const netmaskInputRef = useRef<HTMLInputElement>(null);

  // focus corresponding ip input if index is within 0-3, otherwise focus on netmask input.
  const focusInput = (index: number) =>
    pipe(
      ipInputRefs.current,
      A.lookup(index),
      O.fold(
        () => netmaskInputRef.current?.focus(),
        (input) => input.current?.focus(),
      ),
    );

  /**
   * Generate string for IP CIDR address if ip is not empty.
   * Netmask is optional since it will return a default value 32 if it's empty.
   *
   * Example 1:
   * ```
   * input:
   * (
   *  ip: ["192", "168", "1", "1"],
   *  netmask: "24"
   * )
   *
   * output
   * `192.168.1.1/24`
   * ```
   *
   * Example 2:
   * ```
   * input:
   * (
   *  ip: ["192", "168", "1", "1"],
   *  netmask: ""
   * )
   *
   * output
   * `192.168.1.1/32`
   * ```
   */
  const generateIPV4CIDRString = (
    ip: RNEA.ReadonlyNonEmptyArray<string>,
    netmask: string,
  ): O.Option<string> =>
    pipe(
      ip,
      O.fromPredicate(RA.every(not(S.isEmpty))),
      O.map(RNEA.intercalate(S.Monoid)(".")),
      O.map(
        (ipResult: string) =>
          `${ipResult}/${S.isEmpty(netmask) ? defaultNetmask : netmask}`,
      ),
    );

  const isEmptyOrInRange = (min: number, max: number) => (text: string) =>
    S.isEmpty(text) ||
    pipe(
      text,
      parseInt,
      O.fromPredicate(N.isNumber), // deal with NaN
      O.map((n) => n >= min && n <= max),
      O.getOrElse(constFalse),
    );

  const handleOnChanged = (
    newIP: RNEA.ReadonlyNonEmptyArray<string>,
    newNetmask: string,
  ) => {
    setIPAddress(newIP);
    setNetmask(newNetmask);

    // trigger onChange event if all inputs has valid value
    pipe(generateIPV4CIDRString(newIP, newNetmask), O.map(onChange));
  };

  const handleIpChanged = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    index: number,
  ) => {
    const newIpUnit = event.target.value;

    // if input value has 3 digits, or it's 2 digits but larger than 25, focus on next input
    if (
      /^(\d{3})$/.test(newIpUnit) ||
      (/^(\d{2})$/.test(newIpUnit) && parseInt(newIpUnit) > 25)
    ) {
      focusInput(index + 1);
    }

    // update ip address state
    pipe(
      newIpUnit,
      // accept empty string or 0 ~ 255
      O.fromPredicate(isEmptyOrInRange(0, 255)),
      O.map((ipUnit: string) =>
        pipe(
          ipAddress,
          RNEA.updateAt(index, ipUnit),
          O.map((newIp) => handleOnChanged(newIp, netmask)),
        ),
      ),
    );
  };

  const handleNetmaskChanged = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) =>
    pipe(
      event.target.value,
      // accept empty string or 0 ~ 32
      O.fromPredicate(isEmptyOrInRange(0, 32)),
      O.map((newNetmask: string) => handleOnChanged(ipAddress, newNetmask)),
    );

  /**
   * Handle key down event for each input.
   *
   * @param event key down event.
   * @param index index represents the input.
   * @param inputValue current value for input.
   */
  const handleKeyDownEvent = (
    event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>,
    index: number,
    inputValue: string,
  ) =>
    pipe(
      event.code,
      KeyCodeTypesUnion.decode,
      E.fold(
        console.error,
        simpleUnionMatch<KeyCodeTypes, void>({
          Enter: () => focusInput(index + 1),
          Backspace: () => {
            if (S.isEmpty(inputValue)) {
              focusInput(index - 1);
            }
          },
          Period: () => {
            if (!S.isEmpty(inputValue)) {
              focusInput(index + 1);
              // key `Period` will trigger focus event on current input, thus prevent it.
              event.preventDefault();
            }
          },
          NumpadDecimal: () => {
            if (!S.isEmpty(inputValue)) {
              focusInput(index + 1);
              // key `NumpadDecimal` will trigger focus event on current input.
              event.preventDefault();
            }
          },
        }),
      ),
    );

  const ipInput = (index: number) => {
    const currentValue = pipe(
      ipAddress as readonly string[],
      RA.lookup(index),
      O.getOrElse(constant("")),
    );

    return (
      <TextField
        id={`ip-${index}-input`}
        size="small"
        type="tel"
        inputProps={{
          onKeyDown: (event) => handleKeyDownEvent(event, index, currentValue),
        }}
        inputRef={ipInputRefs.current[index]}
        className={classes.ipInput}
        placeholder="255"
        variant="outlined"
        value={currentValue}
        onChange={(e) => handleIpChanged(e, index)}
      />
    );
  };

  const dot = () => <span className={classes.infix}>.</span>;

  const wrapGrid = (index: number, children: React.JSX.Element) => (
    <Grid key={index}>{children}</Grid>
  );

  return (
    <StyledGrid container alignItems="flex-end">
      {pipe(
        A.makeBy(ipElements, ipInput),
        A.intersperse(dot()),
        A.mapWithIndex(wrapGrid),
      )}
      <Grid className={classes.infix}>/</Grid>
      <Grid>
        <TextField
          size="small"
          id="netmask-input"
          type="tel"
          inputRef={netmaskInputRef}
          inputProps={{
            onKeyDown: (key) => {
              handleKeyDownEvent(key, ipElements, netmask);
            },
          }}
          className={classes.ipInput}
          placeholder={defaultNetmask.toString()}
          variant="outlined"
          value={netmask}
          onChange={handleNetmaskChanged}
        />
      </Grid>
    </StyledGrid>
  );
};

export default IPv4CIDRInput;
