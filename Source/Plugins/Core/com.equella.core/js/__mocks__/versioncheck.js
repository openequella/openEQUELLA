"use strict";

var releaseCheckUrl =
  "https://api.github.com/repos/openequella/openEQUELLA/releases";
var semanticVersionPattern = new RegExp(/^(\d+?)\.(\d+?)\.(\d+?)$/);

function checkVersion(currentVersion, callback) {
  $.ajax(releaseCheckUrl).done(function(data) {
    console.log(data);
    var checkResult = createCheckResult(currentVersion, data);
    callback(checkResult.newer, JSON.stringify(checkResult.newerReleases));
  });
}

function createCheckResult(currentVersion, data) {
  var newerReleaseFound = false;
  var newerReleases = null;
  var currentVersionMatched = currentVersion.match(semanticVersionPattern);

  if (currentVersionMatched) {
    var currentVersionMajor = currentVersionMatched[1];
    var currentVersionMinor = currentVersionMatched[2];
    var currentVersionPatch = currentVersionMatched[3];
    var releaseList = getReleaseList(data);
    var latestMajorRelease = getLatestMajorRelease(
      releaseList,
      currentVersionMajor
    );
    var latestMinorRelease = getLatestMinorRelease(
      releaseList,
      currentVersionMajor,
      currentVersionMinor
    );
    var latestPatchRelease = getLatestPatchRelease(
      releaseList,
      currentVersionMajor,
      currentVersionMinor,
      currentVersionPatch
    );
    newerReleaseFound =
      latestMajorRelease != null ||
      latestMinorRelease != null ||
      latestPatchRelease != null;
    newerReleases = {
      majorUpdate: latestMajorRelease,
      minorUpdate: latestMinorRelease,
      patchUpdate: latestPatchRelease
    };
  }

  return {
    newer: newerReleaseFound,
    newerReleases: newerReleases
  };
}

function getReleaseList(data) {
  var releaseList = [];
  data.forEach(function(value) {
    var releaseVersion = value.name;
    var releaseUrl = value.html_url;
    var releaseVersionMatched = releaseVersion.match(semanticVersionPattern); // We only want releases which support the semantic version

    if (releaseVersionMatched) {
      var releaseVersionMajor = releaseVersionMatched[1];
      var releaseVersionMinor = releaseVersionMatched[2];
      var releaseVersionPatch = releaseVersionMatched[3];
      releaseList.push({
        major: releaseVersionMajor,
        minor: releaseVersionMinor,
        patch: releaseVersionPatch,
        url: releaseUrl
      });
    }
  });
  return releaseList;
}

function getLatestMajorRelease(releaseList, currentVersionMajor) {
  // Find out all major releases published after current version
  var majorReleaseList = releaseList.filter(function(release) {
    return release.major > currentVersionMajor;
  });

  if (majorReleaseList.length > 0) {
    // Find out the latest major release
    var latestMajorRelease = majorReleaseList.reduce(function(
      release1,
      release2
    ) {
      var release1Major = release1.major;
      var release1Minor = release1.minor;
      var release1Patch = release1.patch;
      var release2Major = release2.major;
      var release2Minor = release2.minor;
      var release2Patch = release2.patch;
      var newerMajorFound = release1Major < release2Major;
      var newerMinorFound =
        release1Major === release2Major && release1Minor < release2Minor;
      var newerPatchUpFound =
        release1Major === release2Major &&
        release1Minor === release2Minor &&
        release1Patch < release2Patch;

      if (newerMajorFound || newerMinorFound || newerPatchUpFound) {
        return release2;
      }

      return release1;
    });
    return latestMajorRelease;
  }

  return null;
}

function getLatestMinorRelease(
  releaseList,
  currentVersionMajor,
  currentVersionMinor
) {
  // Find out all minor releases published after current version
  var minorReleaseList = releaseList.filter(function(release) {
    return (
      release.major === currentVersionMajor &&
      release.minor > currentVersionMinor
    );
  });

  if (minorReleaseList.length > 0) {
    // Find out the latest minor release
    var latestMinorRelease = minorReleaseList.reduce(function(
      release1,
      release2
    ) {
      var release1Minor = release1.minor;
      var release1Patch = release1.patch;
      var release2Minor = release2.minor;
      var release2Patch = release2.patch;
      var newerMinorFound = release1Minor < release2Minor;
      var newerPatchUpFound =
        release1Minor === release2Minor && release1Patch < release2Patch;

      if (newerMinorFound || newerPatchUpFound) {
        return release2;
      }

      return release1;
    });
    return latestMinorRelease;
  }

  return null;
}

function getLatestPatchRelease(
  releaseList,
  currentVersionMajor,
  currentVersionMinor,
  currentVersionPatch
) {
  // Find out all patch releases published after current version
  var patchReleaseList = releaseList.filter(function(release) {
    return (
      release.major === currentVersionMajor &&
      release.minor === currentVersionMinor &&
      release.patch > currentVersionPatch
    );
  });

  if (patchReleaseList.length > 0) {
    // Find out the latest patch release
    var latestPatchRelease = patchReleaseList.reduce(function(
      release1,
      release2
    ) {
      var release1Patch = release1.patch;
      var release2Patch = release2.patch;

      if (release1Patch < release2Patch) {
        return release2;
      }

      return release1;
    });
    return latestPatchRelease;
  }

  return null;
}

exports.createCheckResult = createCheckResult;
