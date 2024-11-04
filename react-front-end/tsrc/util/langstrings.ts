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

declare let bundle: { [prefix: string]: string };

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

export interface LanguageStrings {
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
  strings: LanguageStrings | string,
): LanguageStrings | string {
  if (typeof bundle == "undefined") return strings;
  const overrideVal = (prefix: string, val: LanguageStrings | string) => {
    if (typeof val == "string") {
      const overriden = bundle[prefix];
      if (overriden !== undefined) {
        return overriden;
      }
      return val;
    } else {
      const newOut: LanguageStrings = {};
      for (const key in val) {
        if (Object.prototype.hasOwnProperty.call(val, key)) {
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
    (languageStrings as LanguageStrings)[key] = prepLangStrings(
      key,
      languageStrings[key],
    );
  }
}

export const languageStrings = {
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
  aclterms: {
    title: {
      ugr: "Select User / Group / Role",
      ip: "Select IP range",
      referrer: "HTTP referrer",
      token: "Select shared secret",
    },
  },
  aclExpressionBuilder: {
    match: "Match",
    type: "Type",
    addGroup: "Add grouping",
    homeTab: "Home",
    otherTab: "Other",
    searchButton: "Search",
    searchFilters: {
      users: "Users",
      roles: "Roles",
      groups: "Groups",
    },
    otherACLDescriptions: {
      everyone: "Everyone",
      owner: "The owner of the targeted object",
      logged: "Logged in users",
      guest: "Guest users",
      sso: "Single signed on with identifier",
      ip: "Add an IP Address",
      referrer: "Add a HTTP Referrer",
      referrerLabel: "example.com",
      exactReferrer: "Only match this exact referrer",
      containReferrer: "Match referrers containing this value",
    },
    otherACLTypes: {
      everyone: "Everyone",
      owner: "Owner",
      logged: "Logged in user",
      guest: "Guest",
      sso: "SSO",
      ip: "IP",
      referrer: "HTTP Referrer",
    },
    operators: {
      and: "And",
      or: "Or",
      not: "None",
    },
    errors: {
      ssoTokensNotFound: "No SSO tokens were found",
      ssoTokensFailed: "Failed to get SSO tokens",
    },
  },
  aclExpressionBuilderDialog: {
    title: "Select recipients",
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
    link: "https://github.com/apereo/openEQUELLA-admin-console-package/releases",
  },
  common: {
    action: {
      accept: "Accept",
      add: "Add",
      apply: "Apply",
      browse: "Browse...",
      cancel: "Cancel",
      clear: "Clear",
      close: "Close",
      copy: "Copy",
      delete: "Delete",
      disable: "Disable",
      discard: "Discard",
      dismiss: "Dismiss",
      done: "Done",
      edit: "Edit",
      enable: "Enable",
      no: "No",
      ok: "OK",
      openInNewWindow: "Open in new window",
      openInNewTab: "Open in new tab",
      refresh: "Refresh",
      reject: "Reject",
      register: "Register",
      removeAll: "Remove all",
      resettodefault: "Reset to Default",
      revertchanges: "Revert Changes",
      save: "Save",
      search: "Search",
      select: "Select",
      selectAll: "Select all",
      selectNone: "Select none",
      showLess: "Show less",
      showMore: "Show more",
      undo: "Undo",
      view: "View",
      yes: "Yes",
    },
    breadcrumb: "breadcrumb",
    required: "* Required",
    result: {
      success: "Saved successfully.",
      fail: "Failed to save.",
      errors: "Some changes are not saved due to errors listed below",
    },
    users: "Users",
    groups: "Groups",
    roles: "Roles",
    thumbnail: "Thumbnail",
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
      help: "When accessing EQUELLA via this course in an external system, all resources added to the external system will use this version selection strategy",
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
        text: "By proceeding with this registration you are acknowleding that you agree to the terms and conditions of the ",
        title: "Cloud provider disclaimer",
      },
    },
    deletecloudprovider: {
      title: "Are you sure you want to delete cloud provider - '%s'?",
      message: "It will be permanently deleted.",
    },
    refreshed: "Completed refresh",
  },
  dateRangeSelector: {
    defaultStartDatePickerLabel: "From",
    defaultEndDatePickerLabel: "To",
    defaultDropdownLabel: "Quick date ranges",
    quickOptionSwitchLabel: "Enable quick options",
    quickOptionLabels: {
      all: "All",
      today: "Today",
      lastSevenDays: "Last seven days",
      lastMonth: "Last month",
      thisYear: "This year",
    },
  },
  drm: {
    getTerms: {
      error: "Failed to retrieve DRM terms",
    },
    violation: {
      title: "DRM Violation",
      prefix:
        "You are currently unable to access this item due to the following DRM violation: \n",
    },
  },
  embedCode: {
    label: "Embed code",
    copy: "Copy embed code",
    closeEmbedCodeDialog: "Close embed code dialog",
  },
  entity: {
    edit: {
      tab: {
        permissions: "Permissions",
      },
    },
  },
  error: {
    accessdenied: {
      title: "Access Denied",
      message: "No permission to access %s - missing ACL(s): %s",
    },
  },
  errorpage: {
    title: "Error",
  },
  fileUploader: {
    noFileSelected: "No attached resources",
    failedToDelete: "Failed to delete '%s' due to error: %s",
  },
  hierarchy: {
    viewHierarchy: "View hierarchy",
    browseHierarchyPageTitle: "Browse hierarchies",
    noHierarchy: "No hierarchies available",
    expandHierarchy: "Expand hierarchy",
    collapseHierarchy: "Collapse hierarchy",
    browse: "Browse",
    addKeyResource: "Add as a key resource",
    removeKeyResource: "Remove key resource",
    hierarchyPageTitle: "Hierarchy",
    attachmentCount: "Attachment count",
    error: {
      missingTopicUUID: "Failed to open Hierarchy page: topic ID is not found.",
    },
  },
  invalidLuceneQuery:
    "Your query is invalid. Try simplifying your query to only contain basic terms, and check that you do not have any whitespace around '*' or '+' characters.",
  lightboxComponent: {
    kalturaExternalIdIssue:
      "There is an issue with the format of the externalId for the the Kaltura Video",
    kalturaMissingId:
      "The provided Kaltura media is missing externalId details",
    openSummaryPage: "Open resource summary page",
    unsupportedContent: "Provided content is not supported",
    viewNext: "View next attachment",
    viewPrevious: "View previous attachment",
    youTubeVideoMissingId: "The provided YouTube video is missing a video ID",
  },
  loginnoticepage: {
    title: "Login notice editor",
    clear: {
      title: "Warning",
      confirm: "Are you sure you want to clear this login notice?",
    },
    preLogin: {
      title: "Before login notice",
    },
    postLogin: {
      title: "After login notice",
      description:
        "Write a plaintext message to be displayed after login as an alert...",
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
  myResources: {
    title: "My resources",
    resourceType: {
      published: "Published",
      drafts: "Drafts",
      scrapbook: "Scrapbook",
      modqueue: "Moderation queue",
      archive: "Archive",
      all: "All resources",
    },
    moderating: {
      since: "Moderating since",
    },
    moderationItemTable: {
      ariaLabel: "Table of items in moderation",
      colLastActionDate: "Last action",
      colStatus: "Status",
      colSubmittedDate: "Submitted",
      colTitle: "Title",
      rejectionCommentButton: "View rejection message",
      rejectionCommentDialogTitle: "Rejection reason",
    },
    scrapbook: {
      addScrapbook: "Add to Scrapbook",
      createFile: "Upload files",
      createPage: "Author new web pages",
      deleteDialogTitle: "Delete item?",
      deleteDialogContent: "Are you sure you want to delete this item?",
    },
    sortOptions: {
      dateCreated: "Date created",
      lastAction: "Last action",
      lastModified: "Date last modified",
      relevance: "Relevance",
      submitted: "Submitted",
      title: "Title",
      userRating: "User rating",
    },
  },
  navigationguard: {
    title: "Close without saving?",
    message:
      "You have unsaved changes. Are you sure you want to leave this page without saving?",
  },
  newuisettings: {
    title: "Theme Settings",
    colourschemesettings: {
      title: "Colour Scheme",
      primarycolour: "Primary Colour",
      menubackgroundcolour: "Menu Background Colour",
      backgroundcolour: "Background Colour",
      paperColor: "Paper Colour",
      secondarycolour: "Secondary Colour",
      sidebartextcolour: "Sidebar Text Colour",
      primarytextcolour: "Primary Text Colour",
      secondarytextcolour: "Secondary Text Colour",
      sidebariconcolour: "Icon Colour",
    },
    logoSettings: {
      alt: "Logo",
      title: "Logo Settings",
      siteLogo: "Site Logo",
      siteLogoDescription:
        "The main logo for the site, primarily displayed in the top left of pages. (Use a PNG file with a maximum width of 230 pixels for best results.)",
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
  screenoptions: {
    description: "Screen options",
  },
  searchpage: {
    deadAttachmentWarning:
      "This attachment appears to be broken or inaccessible.",
    loading: "Loading search results...",
    modifiedDate: "Modified",
    newSearch: "New search",
    newSearchHelperText: "Clears search text and filters",
    noResultsFound: "No results found.",
    refineTitle: "Refine search",
    resultsAvailable: "results available",
    shareSearchConfirmationText: "Search link saved to clipboard",
    shareSearchHelperText: "Copy search link to clipboard",
    showAdvancedSearchFilter: "Show advanced search filters",
    subtitle: "Search results",
    title: "Search",
    wildcardSearch: "Wildcard search",
    AdvancedSearchPanel: {
      title: "Advanced Search",
      duplicateTargetWarning:
        "Warning: This Advanced search has multiple matching control types pointing to the same metadata schema node.",
    },
    displayModeSelector: {
      title: "Display mode",
      modeItemList: "Standard",
      modeGalleryImage: "Image gallery",
      modeGalleryVideo: "Video gallery",
    },
    collectionSelector: {
      noOptions: "All",
      title: "Collections",
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
    categorySelector: {
      title: "Classifications",
    },
    export: {
      collectionLimit: "Download limited to one collection.",
      exportCompleted: "File downloaded",
      title: "Download search result to a CSV file",
      errorMessages: {
        badRequest:
          "Export failed due to the server indicating the request was invalid.",
        unauthorised: "Sorry, you are not authorised to export this search.",
        notFound: "Failed to find details to export this search.",
      },
    },
    favouriteItem: {
      add: "Add to favourites",
      remove: "Remove from favourites",
      removeAlert: "Are you sure you want to remove from your favourites?",
      tagDescription: "Tags to help when searching (optional).",
    },
    addToHierarchy: {
      title: "Add as key resource to a hierarchy",
      dialogTitle: "Modify key resource",
      dialogDesc:
        "Click add/remove icon to update the key resource for the hierarchy",
      add: "Add to hierarchy",
      remove: "Remove from hierarchy",
    },
    hierarchyKeyResourceDialog: {
      add: "Add to key resource",
      remove: "Remove from key resource",
      removeAlert: "Are you sure you want to remove from key resource?",
    },
    favouriteSearch: {
      saveSearchConfirmationText: "Search added to favourites",
      saveSearchFailedText: "Failed to save new favourite, please try again",
      text: "Please enter a name for this search",
      title: "Add search to favourites",
    },
    filterOwner: {
      title: "Owner",
      chip: "Owner: ",
      clear: "Clear owner selector",
      selectTitle: "Select user to filter by",
    },
    lastModifiedDateSelector: {
      title: "Date modified",
      startDatePicker: "Modified after",
      endDatePicker: "Modified before",
      quickOptionDropdown: "Last modified date",
    },
    mimeTypeFilterSelector: {
      title: "Filter by Attachment type",
      helperText: "Attachment types",
    },
    order: {
      relevance: "Relevance",
      name: "Name",
      datemodified: "Date modifed",
      datecreated: "Date created",
      rating: "Rating",
    },
    pagination: {
      firstPageButton: "First page",
      previousPageButton: "Previous page",
      nextPageButton: "Next page",
      lastPageButton: "Last page",
      itemsPerPage: "Items per page",
    },
    refineSearchPanel: {
      title: "Refine search",
    },
    advancedSearchSelector: {
      label: "Advanced searches",
      title: "Access Advanced searches",
    },
    remoteSearchSelector: {
      title: "Access Remote repositories",
      label: "Remote repositories",
    },
    searchAttachmentsSelector: {
      title: "Search attachments",
    },
    searchBarPlaceholder: "Search",
    searchResult: {
      ariaLabel: "Search result list item",
      attachments: "Attachments",
      dateModified: "Modified",
      attachmentLink: "Attachment link",
      keywordFoundInAttachment: "Search term found in attachment content",
      errors: {
        getAttachmentViewerDetailsFailure:
          "Failed to get attachment viewer details",
      },
    },
    gallerySearchResult: {
      ariaLabel: "Search result gallery item",
      viewItem: "View item",
    },
    statusSelector: {
      all: "All",
      live: "Live",
      title: "Status",
    },
    thumbnails: {
      html: "HTML Icon",
      placeholder: "Placeholder Icon",
      provided: "Provided Icon",
      file: "Default File Icon",
      image: "Image Icon",
      video: "Video Icon",
      link: "Link Icon",
      item: "Item Icon",
    },
    comments: {
      zero: "No comments",
      one: "%d comment",
      more: "%d comments",
    },
    starRatings: {
      label: "Item star rating: %f",
    },
    selectResource: {
      summaryPage: "Select summary page",
      attachment: "Select attachment",
      allAttachments: "Select all attachments",
    },
  },
  selectItemVersionDialog: {
    selectVersion: "Select version to add:",
    toThisVersion:
      "Note: Once this item is added, it will to point to this version permanently.",
    versionOptions: {
      useLatestVersion: "Always use latest version",
      useThisVersion: "This version",
    },
  },
  settings: {
    general: { name: "General", desc: "General settings" },
    integration: {
      name: "Integrations",
      desc: "Settings for integrating with external systems",
      lti13PlatformsSettings: {
        name: "LTI 1.3 platforms settings",
        platformsTitle: "Platforms",
        platformsDesc:
          "LTI 1.3 Platforms are digital learning environments or systems such as a Learning Management System (LMS) that can interact with LTI 1.3 Tools (in this case, openEQUELLA) to provide resources to users.",
        enabledSwitch: "Enable/Disable",
        providerDetailsTitle: "LTI 1.3 Provider Details",
        providerDetailsDesc:
          "Below are the details for this openEQUELLA institution's LTI 1.3 provider interface. Please use these to configure your LTI 1.3 Platform.",
        providerDetails: {
          toolUrl: "Tool Url",
          keysetUrl: "Keyset URL",
          initialLoginUrl: "Initial Login URL",
          redirectionUrl: "Redirection URL",
          contentSelectionUrl: "Content Selection URL",
        },
        createPage: {
          name: "Create LTI 1.3 platform",
          generalDetails: {
            title: "General Details",
            platformId: "Platform ID",
            name: "Name",
            clientId: "Client ID",
            needUrl: "URL must include http/https protocol",
            platformKeysetURL: "Platform Keyset URL",
            platformAuthenticationRequestURL:
              "Platform Authentication Request URL",
            usernameClaim: "Custom username claim",
            usernameClaimDesc:
              "A JWT claim provided during the LTI Authentication to determine the user’s username. If not specified, the standard claim 'sub' will be used. Each path of the claim must be placed within a pair of square brackets. For example: [https://purl.imsglobal.org/spec/lti/claim/lis][person_sourcedid]",
            usernamePrefix: "Username Prefix",
            usernameSuffix: "Username Suffix",
          },
          accessControl: {
            title: "Access Control",
            usableBy: "Usable by",
            usableByDesc:
              "Only allow user matching this expression to use this platform",
            unknownUserHandling: "Unknown user handling",
            unknownUserHandlingDesc:
              "Choose how to handle logon attempts with a username that doesn't exist",
            unknownUserHandlingDeny: "Deny access and present error message",
            unknownUserHandlingGuest: "Treat user as a guest",
            unknownUserHandlingCreate:
              "Create local user and add them to the following groups",
            groups: "Select groups",
          },
          roleMappings: {
            title: "Role Mappings",
            selectRole: "Select role",
            instructorRoles: "Instructor roles",
            instructorRolesDesc:
              "Select the openEQUELLA roles that should be used when the LTI Instructor role is detected",
            customRoles: "Custom roles",
            customRolesDesc:
              "Select an LTI role, then select the openEQUELLA roles",
            customRoleDialogTitle: "Select LTI and openEQUELLA roles",
            customRoleSelectLtiRoleLabel: "Select LTI role",
            customRoleTableLtiRoleColumn: "LTI Role Name",
            unknownRoles: "Unknown roles",
            unknownRolesDesc:
              "Select the openEQUELLA roles that should be used for all unmapped LTI roles",
          },
          // The following role names are written according to the LTI 1.3 standard.
          // Reference link: https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
          ltiRoles: {
            system: {
              Administrator: "System Administrator",
              None: "System None",
              AccountAdmin: "System Account Admin",
              Creator: "System Creator",
              SysAdmin: "System Admin",
              SysSupport: "System Support",
              User: "System User",
            },
            institution: {
              Administrator: "Institution Administrator",
              Faculty: "Institution Faculty",
              Guest: "Institution Guest",
              None: "Institution None",
              Other: "Institution Other",
              Staff: "Institution Staff",
              Student: "Institution Student",
              Alumni: "Institution Alumni",
              Instructor: "Institution Instructor",
              Learner: "Institution Learner",
              Member: "Institution Member",
              Mentor: "Institution Mentor",
              Observer: "Institution Observer",
              ProspectiveStudent: "Institution Prospective Student",
            },
            context: {
              Administrator: "Context Administrator",
              ContentDeveloper: "Context Content Developer",
              Instructor: "Context Instructor",
              Learner: "Context Learner",
              Mentor: "Context Mentor",
              Manager: "Context Manager",
              Member: "Context Member",
              Officer: "Context Officer",
            },
          },
        },
        editPage: {
          name: "Edit LTI 1.3 platform",
          wrongURL: "Can't get platform ID from URL",
          savingWarning: "Caution, saving will remove these values.",
          mismatchWarning: "Can't fetch %s for IDs: %s.",
          security: {
            title: "Security",
            keyPair: "Key pair",
            keyPairDesc:
              "Rotating your key pair is an important security measure that helps ensure the integrity of your LTI 1.3 platform",
            rotateKeyPair: "Rotate Key Pair",
            rotateKeyPairConfirmText: "Rotate Key Pair?",
            rotateKeyPairSuccess: "Key pair rotated successfully",
          },
        },
      },
      oidc: {
        name: "OIDC configuration",
        generalDetails: {
          title: "General Details",
          platform: "Platform",
          issuer: "Issuer",
          issuerDesc:
            "The issuer identifier for the OpenID Connect provider. This value should match the 'iss' claim in the JWTs issued by this provider.",
          authCodeClientId: "Client ID",
          authCodeClientSecret: "Client secret",
          authUrl: "Identity Provider Login URL",
          authUrlDesc:
            "Enter the URL of your Identity Provider's authorization endpoint. This is where users will be redirected to initiate the OpenID Connect login flow.",
          keysetUrl: "Public Key Endpoint URL",
          keysetUrlDesc:
            "Enter the URL of the endpoint providing the JSON Web Key Set (JWKS). This set of public keys is used to verify the authenticity of tokens issued by the Identity Provider.",
          tokenUrl: "Token URL",
          tokenUrlDesc:
            "Enter the URL of the token endpoint. openEQUELLA will use this (with the above Client ID and Secret) to obtain an ID token to identify the authenticated user.",
          usernameClaim: "Username claim",
          usernameClaimDesc:
            "By default, openEQUELLA uses the 'sub' claim from the ID token as the username. To use a different claim (e.g., email or preferred_username), enter the claim name here. Leave blank to use the default 'sub' claim.",
          enable: "Enable",
        },
        roleMappings: {
          title: "Role Mappings",
          defaultRole: "Default roles",
          defaultRoleDesc:
            "Map the openEQUELLA roles to be used for users logging in via the Identity Provider.",
          roleClaim: "Role claim",
          roleClaimDesc:
            "Specify a custom claim provided by your Identity Provider (IdP) in the ID Token to represent the user's role. This allows you to map IdP roles to openEQUELLA roles for authorization (Role mapping options will be displayed once specified).",
          customRoleDialog: {
            title: "IdP role mappings",
            customRoleLabel: "Enter IdP role",
            customRoleColumnName: "IdP Role Name",
          },
        },
        oeqDetails: {
          title: "openEquella (RP) details",
          desc: "Below is the details for this openEQUELLA institution's OIDC interface. Please use this to configure your OIDC Platform.",
          redirect: "Redirection URL",
        },
      },
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
        allowStatusControl: "Enable status selector",
        allowStatusControlLabel:
          "Allow users to toggle between live and all statuses via the status selector",
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
        add: "Create classification",
        delete: "Delete classification",
        edit: "Edit classification",
        explanationText:
          "Classifications and their categories display in the Refine search panel of the Search page.",
        facetfields: {
          categoryNumber: "Default number of categories",
          categoryNumberHelper: "Leave blank to display all categories",
          name: "Classification name",
          nameHelper: "Enter name to display in the Refine search panel",
          schemaNode: "Schema node",
          schemaNodeHelper:
            "The categories will be generated from the selected node",
        },
        name: "Faceted search settings",
        schemaSelector: {
          nodeSelector: {
            expandAll: "Expand All",
            collapseAll: "Collapse All",
          },
          permissionsHelperText:
            "The LIST_SCHEMA permission is required to select a schema",
          schema: "Schema",
          selectASchema: "Select a schema...",
        },
        subHeading: "Classifications",
      },
    },
    ui: { name: "UI", desc: "UI settings" },
  },
  shuffleBox: {
    addAll: "Add all",
    addSelected: "Add selected",
    availableOptions: "Available options",
    currentSelections: "Current selections",
    removeAll: "Remove all",
    removeSelected: "Remove selected",
  },
  shuffleList: {
    newEntry: "New entry",
    valueList: "Current list of values",
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
      usernameUnknown: "Username unknown",
      help: "Help",
    },
  },
  termSelector: {
    placeholder: "Search term",
    loadingText: "Searching terms...",
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
  selectCustomRoleDialog: {
    title: "Select Custom and openEQUELLA roles",
    customRoleTitle: "Input Custom role",
    customRoleLabel: "Enter custom role",
    oeqRoleTitle: "Select openEQUELLA role",
    currentMappings: "Mappings",
    customRoleColumn: "Custom Role Name",
    oeqRoleColumn: "openEQUELLA Role Name",
    addRoles: "Please add roles with the selector and search control",
  },
  customRolesMappingControl: {
    title: "Custom roles",
    desc: "Map the values from your specified role claim to corresponding roles in openEQUELLA",
  },
  selectEntityDialog: {
    currentSelections: "Current selections",
  },
  selectGroupDialog: {
    addGroups: "Please add groups with the search control",
  },
  selectRoleDialog: {
    addRoles: "Please add roles with the search control",
  },
  baseSearchComponent: {
    helpTitle: "Select entity(s)",
    helpDesc: "The search query can contain an entity name or partial name.",
    queryFieldLabel: "Item",
    failedToFindMessage: "Unable to find any item matching '%s'",
    filterActiveNotice: "Results will be filtered.",
    provideQueryMessage: "Please provide a query term",
    filterByGroupsButtonLabel: "Filter by groups",
    filteredByPrelude: "Your search results will be filtered by these groups:",
  },
  userSearchComponent: {
    helpTitle: "Select user(s)",
    helpDesc:
      "The search query can contain a username, first name, last name or partial name.",
    failedToFindMessage: "Unable to find any user matching '%s'",
    queryFieldLabel: "Enter username, first or last name",
  },
  groupSearchComponent: {
    helpTitle: "Select group(s)",
    helpDesc: "The search query can contain a group name or partial name.",
    failedToFindMessage: "Unable to find any group matching '%s'",
    queryFieldLabel: "Search groups",
  },
  roleSearchComponent: {
    helpTitle: "Select role(s)",
    helpDesc: "The search query can contain a role name or partial name.",
    failedToFindMessage: "Unable to find any role matching '%s'",
    queryFieldLabel: "Search roles",
  },
  groupSearchDialog: {
    title: "Select group(s)",
  },
  roleSearchDialog: {
    title: "Select role(s)",
  },
  wizard: {
    controls: {
      unsupported: {
        label: "Unsupported control configured",
        description:
          "An unsupported control has been detected, please contact your system administrator.",
      },
      userSelector: {
        selectUsers: "Select users",
        userList: "Current selection of users",
      },
    },
    options: {
      allOptions: "All",
    },
  },
  youTubePlayer: {
    title: "YouTube video player",
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
  "com.equella.core.searching.search": {
    title: "Search",
  },
  "com.equella.core.comments": {
    anonymous: "Anonymous",
    commentmsg: "Comment",
    entermsg: "Enter a comment",
  },
};
