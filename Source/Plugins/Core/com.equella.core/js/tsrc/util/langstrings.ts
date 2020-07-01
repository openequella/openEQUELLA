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
import { sprintf } from "sprintf-js";

declare let bundle: any;

export interface Sizes {
  zero: string;
  one: string;
  more: string;
}

/**
 * Get appropriate language string based off size of value
 *
 * @param size size or count of value
 * @param strings language strings to choose from
 *
 * TODO: replace with https://github.com/formatjs/react-intl
 */
export function formatSize(size: number, strings: Sizes): string {
  let format;
  switch (size) {
    case 0:
      format = strings.zero;
      break;
    case 1:
      format = strings.one;
      break;
    default:
      format = strings.more;
      break;
  }
  return sprintf(format, size);
}

interface LanguageStrings {
  [key: string]: string | LanguageStrings;
}

/**
 * Add prefix to language strings
 *
 * @param prefix prefix to add
 * @param strings language string or object to process
 *
 * TODO: replace with https://github.com/formatjs/react-intl
 */
export function prepLangStrings(
  prefix: string,
  strings: LanguageStrings | string
): LanguageStrings | string {
  if (typeof bundle == "undefined") return strings;
  const overrideVal = (prefix: string, val: LanguageStrings | string) => {
    if (typeof val == "string") {
      const overriden = bundle[prefix];
      if (overriden != undefined) {
        return overriden;
      }
      return val;
    } else {
      const newOut: LanguageStrings = {};
      for (const key in val) {
        if (val.hasOwnProperty(key)) {
          newOut[key] = overrideVal(prefix + "." + key, val[key]);
        }
      }
      return newOut;
    }
  };
  return overrideVal(prefix, strings);
}

export function initStrings() {
  for (const key of Object.keys(languageStrings) as Array<
    keyof typeof languageStrings
  >) {
    prepLangStrings(key, languageStrings[key]);
  }
}

export const languageStrings = {
  cp: {
    title: "Cloud providers",
    cloudprovideravailable: {
      zero: "No cloud providers available",
      one: "%d cloud provider",
      more: "%d cloud providers",
    },
    newcloudprovider: {
      title: "Register a new cloud provider",
      label: "URL",
      text: "Enter the URL supplied by the cloud provider",
      help: "The URL should start with either http:// or https://",
      disclaimer: {
        text:
          "By proceeding with this registration you are acknowleding that you agree to the terms and conditions of the ",
        title: "Cloud provider disclaimer",
      },
    },
    deletecloudprovider: {
      title: "Are you sure you want to delete cloud provider - '%s'?",
      message: "It will be permanently deleted.",
    },
    refreshed: "Completed refresh",
  },
  courseedit: {
    title: "Editing course - %s",
    newtitle: "Creating new course",
    tab: "Course details",
    name: {
      label: "Name",
      help: "Course name, e.g. Advanced EQUELLA studies",
    },
    description: {
      label: "Description",
      help: "A brief description",
    },
    code: {
      label: "Code",
      help: "Course code, e.g. EQ101",
    },
    type: {
      label: "Course Type",
      i: "Internal",
      e: "External",
      s: "Staff",
    },
    department: {
      label: "Department name",
    },
    citation: {
      label: "Citation",
    },
    startdate: {
      label: "Start date",
    },
    enddate: {
      label: "End date",
    },
    version: {
      label: "Version selection",
      default: "Institution default",
      forcecurrent:
        "Force selection to be the resource version the user is viewing",
      forcelatest:
        "Force selection to always be the latest live resource version",
      defaultcurrent:
        "User can choose, but default to be the resource version the user is viewing",
      defaultlatest:
        "User can choose, but default to be the latest live resource version",
      help:
        "When accessing EQUELLA via this course in an external system, all resources added to the external system will use this version selection strategy",
    },
    students: {
      label: "Unique individuals",
    },
    archived: {
      label: "Archived",
    },
    saved: "Successfully saved",
    errored: "Save failed due to server error",
  },
  courses: {
    title: "Courses",
    sure: "Are you sure you want to delete - '%s'?",
    confirmDelete: "It will be permanently deleted.",
    coursesAvailable: {
      zero: "No courses available",
      one: "%d course",
      more: "%d courses",
    },
    includeArchived: "Include archived",
    archived: "Archived",
  },
  entity: {
    edit: {
      tab: {
        permissions: "Permissions",
      },
    },
  },
  loginnoticepage: {
    title: "Login notice editor",
    clear: {
      title: "Warning",
      confirm: "Are you sure you want to clear this login notice?",
    },
    prelogin: {
      label: "Before login notice",
    },
    postlogin: {
      label: "After login notice",
      description:
        "Write a plaintext message to be displayed after login as an alert...",
    },
    notifications: {
      saved: "Login notice saved successfully.",
      cleared: "Login notice cleared successfully.",
      cancelled: "Cancelled changes to login notice.",
    },
    errors: {
      permissions: "You do not have permission to edit these settings.",
    },
    scheduling: {
      title: "Schedule settings",
      start: "Start date:",
      end: "End date:",
      scheduled: "Scheduled",
      alwayson: "On",
      disabled: "Off",
      endbeforestart: "End date must be after start date.",
      expired: "This login notice has expired.",
    },
  },
  template: {
    navaway: {
      title: "You have unsaved changes",
      content: "If you leave this page you will lose your changes.",
    },
    menu: {
      title: "My Account",
      logout: "Logout",
      prefs: "My preferences",
    },
  },
  "com.equella.core": {
    title: "Settings",
    windowtitlepostfix: " | openEQUELLA",
    topbar: {
      link: {
        notifications: "Notifications",
        tasks: "Tasks",
      },
    },
  },
  newuisettings: {
    title: "Theme Settings",
    colourschemesettings: {
      title: "Colour Scheme",
      primarycolour: "Primary Colour",
      menubackgroundcolour: "Menu Background Colour",
      backgroundcolour: "Background Colour",
      secondarycolour: "Secondary Colour",
      sidebartextcolour: "Sidebar Text Colour",
      primarytextcolour: "Primary Text Colour",
      secondarytextcolour: "Secondary Text Colour",
      sidebariconcolour: "Icon Colour",
    },
    logosettings: {
      alt: "Logo",
      title: "Logo Settings",
      imagespeclabel: "Use a PNG file of 230x36 pixels for best results.",
      current: "Current Logo: ",
      nofileselected: "No file selected.",
    },
    errors: {
      invalidimagetitle: "Image Processing Error",
      invalidimagedescription:
        "Invalid image file. Please check the integrity of your file and try again.",
      nofiledescription: "Please select an image file to upload.",
      permissiontitle: "Permission Error",
      permissiondescription: "You do not have permission to edit the settings.",
    },
    colorPicker: {
      dialogTitle: "Select a Color",
    },
  },
  common: {
    action: {
      save: "Save",
      cancel: "Cancel",
      undo: "Undo",
      add: "Add",
      edit: "Edit",
      ok: "OK",
      discard: "Discard",
      select: "Select",
      delete: "Delete",
      search: "Search",
      clear: "Clear",
      close: "Close",
      dismiss: "Dismiss",
      browse: "Browse...",
      apply: "Apply",
      resettodefault: "Reset to Default",
      revertchanges: "Revert Changes",
      register: "Register",
      refresh: "Refresh",
      done: "Done",
    },
    result: {
      success: "Saved successfully.",
      fail: "Failed to save.",
      errors: "Some changes are not saved due to errors listed below",
    },
    users: "Users",
    groups: "Groups",
    roles: "Roles",
  },
  searchpage: {
    title: "Search",
    subtitle: "Search results",
    resultsAvailable: "results available",
    refineTitle: "Refine search",
    modifiedDate: "Modified",
    searchresult: {
      attachments: "Attachments",
      dateModified: "Modified",
    },
    order: {
      relevance: "Relevance",
      name: "Name",
      datemodified: "Date modifed",
      datecreated: "Date created",
      rating: "Rating",
    },
    filterOwner: {
      title: "Owner",
      chip: "Owner: ",
      selectTitle: "Select user to filter by",
    },
    filterLast: {
      label: "Modified within last",
      chip: "Modified within: ",
      name: "Modification date",
      none: "\xa0-\xa0",
      month: "Month",
      year: "Year",
      fiveyear: "Five years",
      week: "Week",
      day: "Day",
    },
  },
  "com.equella.core.searching.search": {
    title: "Search",
  },
  "com.equella.core.comments": {
    anonymous: "Anonymous",
    commentmsg: "Comment",
    entermsg: "Enter a comment",
  },
  uiconfig: {
    facet: {
      name: "Name",
      path: "Path",
      title: "Search facets",
    },
    enableNew: "Enable new UI",
    enableSearch: "Enable new search page",
    themeSettingsButton: "Edit Theme Settings",
  },
  settings: {
    general: { name: "General", desc: "General settings" },
    integration: {
      name: "Integrations",
      desc: "Settings for integrating with external systems",
    },
    diagnostics: { name: "Diagnostics", desc: "Diagnostic pages" },
    searching: {
      name: "Search",
      desc: "Search settings",
      searchPageSettings: {
        name: "Search page settings",
        general: "General",
        defaultSortOrder: "Default sort order",
        defaultSortOrderDesc:
          "The default order that search results are ordered by on the search page",
        relevance: "Relevance",
        lastModified: "Date last modified",
        dateCreated: "Date created",
        title: "Title",
        userRating: "User rating",
        allowNonLive: "Non-live results",
        allowNonLiveLabel:
          "Show the 'Include results that are not live' checkbox",
        authFeed: "Authenticated feeds",
        authFeedLabel: "Generate authenticated RSS and Atom feed links ",
        gallery: "Gallery",
        galleryViews: "Gallery views",
        disableImages: "Disable Images",
        disableImagesDesc: "Removes Images link from results box",
        disableVideos: "Disable Videos",
        disableVideosDesc: "Removes Videos link from results box",
        disableFileCount: "Disable File Count",
        disableFileCountDesc:
          "Removes the file count that displays on each thumbnail in the Images and Videos views",
        cloudSearching: "Cloud searching",
        cloudSearchingLabel:
          "Do not show cloud results when performing searches.",
        disableCloud: "Disable cloud searching",
        save: "Save",
        success: "Settings saved successfully.",
        notFoundError: "Endpoint not found",
        notFoundErrorDesc: "Endpoint not found. Refresh to retry.",
        permissionsError: "You do not have permission to edit these settings.",
      },
      searchfiltersettings: {
        name: "Search filter settings",
        changesaved: "Search filter changes saved successfully",
        mimetypefiltertitle: "Attachment MIME type filters",
        visibilityconfigtitle: "Filter visibility",
        disableownerfilter: "Disable Owner filter",
        disabledatemodifiedfilter: "Disable Date modified filter",
        edit: "Edit MIME type filter",
        add: "Create new MIME type filter",
        delete: "delete MIME type filter",
        save: "save Search filter configurations",
        filternamelabel: "Name",
        mimetypelistlabel: "MIME types *",
      },
      contentIndexSettings: {
        name: "Content indexing",
        description: "Configure how web page attachments are indexed",
        save: "Save",
        general: "General",
        success: "Settings saved successfully.",
        boosting: "Search terms boosting",
        titleBoostingTitle: "Title",
        metaBoostingTitle: "Other metadata",
        attachmentBoostingTitle: "Attachment content",
        option: {
          none: "Do not index",
          webPage: "Web page only",
          secondaryPage: "Web page and linked web pages",
        },
        sliderMarks: {
          off: "Off",
          noBoost: "No boost",
        },
      },
      facetedsearchsetting: {
        name: "Faceted search settings",
        subHeading: "Classifications",
        explanationText:
          "Classifications and their categories display in the Refine search panel of the Search page.",
        add: "Create classification",
        edit: "Edit classification",
        facetfields: {
          name: "Classification name",
          nameHelper: "Enter name to display in the Refine search panel",
          schemaNode: "Schema node",
          schemaNodeHelper:
            "The categories will be generated from the selected node",
          categoryNumber: "Default number of categories",
          categoryNumberHelper: "Leave blank to display all categories",
        },
        schemaSelector: {
          schema: "Schema",
          selectASchema: "Select a schema...",
          nodeSelector: {
            expandAll: "Expand All",
            collapseAll: "Collapse All",
          },
        },
      },
    },
    ui: { name: "UI", desc: "UI settings" },
  },
  adminconsoledownload: {
    id: "adminconsole",
    title: "Administration Console",
    text: {
      introTextOne:
        "The Administration Console is no longer accessed from this link. The Administration Console Package must be ",
      introTextTwo: "downloaded",
      introTextThree:
        " and configured on your system. Once installed, the launcher file is then used to open the openEQUELLA Administration Console Launcher dialog to open the Admin Console.",
    },
    link:
      "https://github.com/apereo/openEQUELLA-admin-console-package/releases",
  },
  aclterms: {
    title: {
      ugr: "Select User / Group / Role",
      ip: "Select IP range",
      referrer: "HTTP referrer",
      token: "Select shared secret",
    },
  },
  acleditor: {
    privilege: "Privilege",
    privileges: "Privileges",
    selectpriv: "Select privilege",
    expression: "Expression",
    privplaceholder: "Please select or add a privilege",
    dropplaceholder: "Drop targets here",
    addpriv: "Add Privilege",
    addexpression: "Add expression",
    targets: "Targets",
    new: {
      ugr: "User, Group or Role",
      ip: "IP Range",
      referrer: "HTTP Referrer",
      token: "Shared secret",
    },
    notted: "NOT - ",
    not: "Not",
    override: "Override",
    revoked: "Revoked",
    revoke: "Revoke",
    required: "* Required",
    match: {
      and: "All match",
      or: "At least one match",
      notand: "Not all match",
      notor: "None match",
    },
    convertGroup: "Convert to group",
  },
  screenoptions: {
    description: "Screen options",
  },
  navigationguard: {
    title: "Close without saving?",
    message:
      "You have unsaved changes. Are you sure you want to leave this page without saving?",
  },
};
