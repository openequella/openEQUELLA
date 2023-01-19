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
import { Literal, Union } from 'runtypes';
import type { UuidString } from './Common';
import type { SelectionRestriction, TermStorageFormat } from './Taxonomy';
import type {
  TargetNode,
  WizardControlOption,
  WizardDateFormat,
} from './WizardCommonTypes';

/**
 * Runtypes definition for Wizard control type.
 */
export const RuntypesControlType = Union(
  Literal('calendar'),
  Literal('checkboxgroup'),
  Literal('editbox'),
  Literal('html'),
  Literal('listbox'),
  Literal('radiogroup'),
  Literal('shufflebox'),
  Literal('shufflelist'),
  Literal('termselector'),
  Literal('userselector')
);

// todo: fix this type alias which is not in sync with the runtype. Jira ticket: OEQ-1438
/**
 * Supported Wizard Control types.
 */
export type ControlType =
  | 'calendar'
  | 'checkboxgroup'
  | 'editbox'
  | 'html'
  | 'listbox'
  | 'radiogroup'
  | 'shufflebox'
  | 'shufflelist'
  | 'termselector'
  | 'userselector';

/**
 * Provide common properties of Wizard Control.
 */
export interface WizardBasicControl {
  /**
   * Whether the control must have a value.
   */
  mandatory: boolean;
  /**
   * Whether to reload all controls after a control has different value.
   */
  reload: boolean;
  /**
   * Whether the control is selectable in Admin Console Advanced Search editor.
   */
  include: boolean;
  /**
   * Number of columns typically used in Radio Button groups and Checkbox groups.
   */
  size1: number;
  /**
   * Number of rows typically used in EditBox.
   */
  size2: number;
  /**
   * The controls' customised name which is used in the Admin Console.
   */
  customName?: string;
  /**
   * Title of the control.
   */
  title?: string;
  /**
   * Description of the control.
   */
  description?: string;
  /**
   * Script which controls the visibility of the control. (Commonly run when rendering control.)
   */
  visibilityScript?: string;
  /**
   * Schema nodes that the control targets to.
   */
  targetNodes: TargetNode[];
  /**
   * Options available for selection. Empty for non-option type controls.
   */
  options: WizardControlOption[];
  /**
   * Default values of control.
   */
  defaultValues: string[];
  /**
   * Text displayed in the criteria summary instead.
   */
  powerSearchFriendlyName?: string;
  /**
   * Type of the control.
   */
  controlType: ControlType;
}

/**
 * Abstract type for controls that support text editing.
 */
interface WizardTextTypeControl extends WizardBasicControl {
  /**
   * Whether each value must be unique.
   */
  isForceUnique: boolean;
  /**
   * Whether to check duplicated values.
   */
  isCheckDuplication: boolean;
}

export interface WizardCalendarControl extends WizardBasicControl {
  /**
   * Whether to support a date range. If false, should only display a single calendar control
   * and supply a single date value. However if true, two controls should be displayed with the
   * values from both representing a range.
   */
  isRange: boolean;
  /**
   * The configured Date format which supports year only, month and year, and day, month and year.
   */
  dateFormat: WizardDateFormat;
  controlType: 'calendar';
}

/**
 * Type guard for WizardShuffleListControl.
 *
 * @param c An object of WizardBasicControl.
 */
export const isWizardShuffleListControl = (
  c: WizardBasicControl
): c is WizardShuffleListControl => c.controlType === 'shufflelist';

export interface WizardShuffleListControl extends WizardTextTypeControl {
  /**
   * Whether to tokenise the value. If true, an '*' must be appended to the schema node in the
   * Lucene query.
   */
  isTokenise: boolean;
  controlType: 'shufflelist';
}

export interface WizardEditBoxControl extends WizardTextTypeControl {
  /**
   * Whether to allow links.
   */
  isAllowLinks: boolean;
  /**
   * Whether to use numbers only.
   */
  isNumber: boolean;
  /**
   * Whether to support multiple languages.
   */
  isAllowMultiLang: boolean;
  controlType: 'editbox';
}

export interface WizardUserSelectorControl extends WizardBasicControl {
  /**
   * Whether to restrict user selection by groups.
   */
  isRestricted: boolean;
  /**
   * Whether selecting multiple users is supported.
   */
  isSelectMultiple: boolean;
  /**
   * Groups which the selection is limited to.
   */
  restrictedTo: UuidString[];
  controlType: 'userselector';
}

export interface WizardTermSelectorControl extends WizardBasicControl {
  /**
   * Whether to allow adding terms.
   */
  isAllowAddTerms: boolean;
  /**
   * Whether to allow multiple terms to be selected.
   */
  isAllowMultiple: boolean;
  /**
   * Which UI to be displayed - TermSelector has three different UI implementations.
   */
  displayType: 'popupBrowser' | 'autocompleteEditBox' | 'widePopupBrowser';
  /**
   * UUID of the selected Taxonomy.
   */
  selectedTaxonomy: UuidString;
  /**
   * The restriction of term selection.
   */
  selectionRestriction: SelectionRestriction;
  /**
   * Whether to search the full taxonomy path or only the selected term.
   * e.g. \a\b\term or term
   */
  termStorageFormat: TermStorageFormat;
  controlType: 'termselector';
}

/**
 * For controls which have types not defined in `ControlType`.
 */
export interface UnknownWizardControl {
  controlType: 'unknown';
}

export type WizardControl =
  | WizardBasicControl
  | WizardCalendarControl
  | WizardEditBoxControl
  | WizardShuffleListControl
  | WizardTermSelectorControl
  | WizardUserSelectorControl
  | UnknownWizardControl;

/**
 * Type guard to narrow the type from WizardControl down to WizardBasicControl.
 *
 * @param control An objet of WizardControl.
 */
export const isWizardBasicControl = (
  control: WizardControl
): control is WizardBasicControl => control.controlType !== 'unknown';
