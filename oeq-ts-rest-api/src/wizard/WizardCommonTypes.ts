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

/**
 * Information about a Wizard target.
 */
export interface TargetNode {
  /**
   * The target node.
   */
  target: string;
  /**
   * Attributes of the target node.
   */
  attribute: string;
  /**
   * Full path of the target node.
   */
  fullTarget: string;
  /**
   * XOQ path of the target node.
   */
  xoqlPath: string;
  /**
   * Free text field of the target node.
   */
  freetextField: string;
}

/**
 * Options of Option' type control such as CheckBox Group and Shuffle List.
 */
export interface WizardControlOption {
  text?: string;
  value: string;
}
