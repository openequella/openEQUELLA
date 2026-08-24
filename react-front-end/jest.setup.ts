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
import { configure } from "@testing-library/react";
import "@html-validate/jest-config";

// testing-library defaults `asyncUtilTimeout` to 1s, which every `waitFor`, `findBy*` and
// `waitForElementToBeRemoved` in the suite inherits. That is too tight for CI runners roughly 3x
// slower per thread than a dev machine - waiting for a mocked search to settle and re-render
// measured ~350ms locally, so the default left barely any margin.
configure({ asyncUtilTimeout: 5000 });
