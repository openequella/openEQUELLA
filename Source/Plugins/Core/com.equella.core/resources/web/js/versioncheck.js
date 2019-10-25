const releaseCheckUrl =
  "https://api.github.com/repos/openequella/openEQUELLA/releases";
const semanticVersionPattern = new RegExp(/^(\d+?)\.(\d+?)\.(\d+?)$/);

function checkVersion(currentVersion, callback) {
  $.ajax(releaseCheckUrl).done(function(data) {
    const checkResult = createCheckResult(currentVersion, data);
    callback(checkResult.newer, JSON.stringify(checkResult.newerReleases));
  });
}

function createCheckResult(currentVersion, data) {
  let newerReleaseFound = false;
  let newerReleases = null;
  const currentVersionMatched = currentVersion.match(semanticVersionPattern);
  if (currentVersionMatched) {
    const currentVersionMajor = currentVersionMatched[1];
    const currentVersionMinor = currentVersionMatched[2];
    const currentVersionPatch = currentVersionMatched[3];

    const releaseList = getReleaseList(data);

    const latestMajorRelease = getLatestMajorRelease(
      releaseList,
      currentVersionMajor
    );
    const latestMinorRelease = getLatestMinorRelease(
      releaseList,
      currentVersionMajor,
      currentVersionMinor
    );
    const latestPatchRelease = getLatestPatchRelease(
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
  return { newer: newerReleaseFound, newerReleases: newerReleases };
}

function getReleaseList(data) {
  const releaseList = [];
  data.forEach(function(value) {
    const releaseVersion = value.name;
    const releaseUrl = value.html_url;
    const releaseVersionMatched = releaseVersion.match(semanticVersionPattern);
    // We only want releases which support the semantic version
    if (releaseVersionMatched) {
      const releaseVersionMajor = releaseVersionMatched[1];
      const releaseVersionMinor = releaseVersionMatched[2];
      const releaseVersionPatch = releaseVersionMatched[3];
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
  const majorReleaseList = releaseList.filter(function(release) {
    return release.major > currentVersionMajor;
  });

  if (majorReleaseList.length > 0) {
    // Find out the latest major release
    const latestMajorRelease = majorReleaseList.reduce((release1, release2) => {
      const release1Major = release1.major;
      const release1Minor = release1.minor;
      const release1Patch = release1.patch;

      const release2Major = release2.major;
      const release2Minor = release2.minor;
      const release2Patch = release2.patch;

      const newerMajorFound = release1Major < release2Major;
      const newerMinorFound =
        release1Major === release2Major && release1Minor < release2Minor;
      const newerPatchUpFound =
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
  const minorReleaseList = releaseList.filter(function(release) {
    return (
      release.major === currentVersionMajor &&
      release.minor > currentVersionMinor
    );
  });

  if (minorReleaseList.length > 0) {
    // Find out the latest minor release
    const latestMinorRelease = minorReleaseList.reduce((release1, release2) => {
      const release1Minor = release1.minor;
      const release1Patch = release1.patch;

      const release2Minor = release2.minor;
      const release2Patch = release2.patch;

      const newerMinorFound = release1Minor < release2Minor;
      const newerPatchUpFound =
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
  const patchReleaseList = releaseList.filter(function(release) {
    return (
      release.major === currentVersionMajor &&
      release.minor === currentVersionMinor &&
      release.patch > currentVersionPatch
    );
  });

  if (patchReleaseList.length > 0) {
    // Find out the latest patch release
    const latestPatchRelease = patchReleaseList.reduce((release1, release2) => {
      const release1Patch = release1.patch;
      const release2Patch = release2.patch;

      if (release1Patch < release2Patch) {
        return release2;
      }

      return release1;
    });
    return latestPatchRelease;
  }
  return null;
}
