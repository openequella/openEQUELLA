function checkVersion(currentVersion, callback) {
  const releaseCheckUrl =
    "https://api.github.com/repos/openequella/openEQUELLA/releases";
  $.ajax(releaseCheckUrl).done(function(data) {
    const checkResult = createCheckResult(currentVersion, data);
    callback(checkResult.newer, JSON.stringify(checkResult.newerReleases));
  });
}

function createCheckResult(currentVersion, data) {
  let newerReleaseFound = false;
  let newerReleases = null;
  const parsedVersion = parseVersion(currentVersion);

  if (parsedVersion != null) {
    const releaseList = getReleaseList(data);

    const latestMajorRelease = getLatestMajorRelease(
      releaseList,
      parsedVersion
    );
    const latestMinorRelease = getLatestMinorRelease(
      releaseList,
      parsedVersion
    );
    const latestPatchRelease = getLatestPatchRelease(
      releaseList,
      parsedVersion
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
  } else {
    console.log(
      `Current version ${currentVersion} does not match the semantic version rule`
    );
  }
  return { newer: newerReleaseFound, newerReleases: newerReleases };
}

function getReleaseList(data) {
  const releaseList = [];
  data.forEach(function(value) {
    const releaseVersion = value.name;
    const releaseUrl = value.html_url;
    const parsedVersion = parseVersion(releaseVersion);
    // We only want releases which support the semantic version
    if (parsedVersion != null) {
      releaseList.push({
        major: parsedVersion.major,
        minor: parsedVersion.minor,
        patch: parsedVersion.patch,
        url: releaseUrl
      });
    }
  });
  return releaseList;
}

function getLatestMajorRelease(releaseList, parsedVersion) {
  // Find out all major releases published after current version
  const majorReleaseList = releaseList.filter(function(release) {
    return release.major > parsedVersion.major;
  });

  if (majorReleaseList.length > 0) {
    // Find out the latest major release
    const latestMajorRelease = majorReleaseList.reduce((release1, release2) => {
      const newerMajorFound = release1.major < release2.major;
      const newerMinorFound =
        release1.major === release2.major && release1.minor < release2.minor;
      const newerPatchFound =
        release1.major === release2.major &&
        release1.minor === release2.minor &&
        release1.patch < release2.patch;

      if (newerMajorFound || newerMinorFound || newerPatchFound) {
        return release2;
      }
      return release1;
    });

    return latestMajorRelease;
  }
  return null;
}

function getLatestMinorRelease(releaseList, parsedVersion) {
  // Find out all minor releases published after current version
  const minorReleaseList = releaseList.filter(function(release) {
    return (
      release.major === parsedVersion.major &&
      release.minor > parsedVersion.minor
    );
  });

  if (minorReleaseList.length > 0) {
    // Find out the latest minor release
    const latestMinorRelease = minorReleaseList.reduce((release1, release2) => {
      const newerMinorFound = release1.minor < release2.minor;
      const newerPatchFound =
        release1.minor === release2.minor && release1.patch < release2.patch;

      if (newerMinorFound || newerPatchFound) {
        return release2;
      }
      return release1;
    });
    return latestMinorRelease;
  }
  return null;
}

function getLatestPatchRelease(releaseList, parsedVersion) {
  // Find out all patch releases published after current version
  const patchReleaseList = releaseList.filter(function(release) {
    return (
      release.major === parsedVersion.major &&
      release.minor === parsedVersion.minor &&
      release.patch > parsedVersion.patch
    );
  });

  if (patchReleaseList.length > 0) {
    // Find out the latest patch release
    const latestPatchRelease = patchReleaseList.reduce((release1, release2) => {
      if (release1.patch < release2.patch) {
        return release2;
      }
      return release1;
    });

    return latestPatchRelease;
  }
  return null;
}

function parseVersion(version) {
  // This regex checks if a version number matches the rule of semantic version.
  // For example, an expected input is '2019.1.1' (not including single quotes).
  // And the expected output is an array: [ '2019.1.1', '2019', '1', '1'].
  const semanticVersionPattern = new RegExp(/^(\d+)\.(\d+)\.(\d+)$/);

  const parsedVersion = version.match(semanticVersionPattern);
  let result = null;
  if (parsedVersion) {
    result = {
      major: parsedVersion[1],
      minor: parsedVersion[2],
      patch: parsedVersion[3]
    };
  }
  return result;
}
