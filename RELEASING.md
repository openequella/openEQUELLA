# openEQUELLA Release Processes

## Overview

This document provides guidance on the processes for the release of openEQUELLA (oEQ). This covers
the two main release types which are done - stable and hotfix.

## Introduction

The oEQ project follows essentially the [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model) for the management of its git repository.
Utilising this there are then two key release types:

- **Stable** - these are releases which originate from the `master` git branch. These first start
  with a new feature release, and then subsequent patches to that until it is superseded by a new
  feature release. The code on `master` is always the current 'stable' version;
- **Hotfixes** (or patches) - these are releases done on previously released stable versions
  (typically only going back two stable releases). The intention with these is that the included
  change in any new hotfix is low risk, but addresses key issues being experienced by adopters.

Hotfixes include code which has also been included on the latest stable version, and has
been deemed safe to 'backport' to older versions. (E.g. a fix may be made to 2019.2.0 resulting in
2019.2.1; and then then it may be chosen to backport to the current 2019.1.x release making 2019.1.3.)

### Versioning

oEQ utilises the [calver versioning scheme](https://calver.org):

    <year>.<release>.<patch>

Each feature release (new 'stable' release) has:

- `year` as per the year it is released;
- an incrementing `release` (starting at 1) represent the feature release for that year; and
- a `patch` of `0`.

For example, the first feature release of 2020 would be `2020.1.0`.

Each subsequent update to the stable release and future hotfixes simply increment the `patch`
number.

For example, the first update to `2020.1.0` will result in `2020.1.1`.

Each version also results in a tag in the git repository to facilitate look up of which codeset
relates to a distribution.

### What about the latest cutting edge code?

The oEQ project does not provide nightly builds, however if people wanted access to the code under
develop leading to the next feature release, this can be found on the `release` branch. However
there are no claims made to the stability of this branch - it's very much at ones own risk, and
meant only for developers.

## Processes

### Stable Releases

Creation of 'stable' releases start when 'feature complete'/'code freeze' has been reached. At this
point a `release` branch is created from `develop`. On this branch first the version information is
updated, and then the first 'release candidate' is built ready for final testing. If issues are
found and deemed requiring fixing prior to release then the subsequent builds of this branch can be
for additional 'release candidates'.

This continues until agreement is reached that the code on the release branch is ready for final
release. At which point version information is again updated (to remove any reference to RC), and
the release branch is merged into `master` and tagged. The build from that tag is then the new
'stable' release. Lastly, that tag point is merged into `develop` to ensure any last minute fixes
etc. also make it back into the next feature release.

#### Step by Step

Starting from a `develop` which is feature complete, assuming we're aiming for version
2020.1.0:

1. Create a new branch (from `develop`) - e.g. `git checkout -b release/2020.1.0`
2. Update root `build.sbt` ensuring `equellaMajor`, `equellaMinor` and `equellaPatch` correctly
   represent the version
3. Update root `build.sbt` to set `equellaStream` is `RC` - for release candidate, consider even
   using `RC1` so that later you can use `RC2` etc for any rework cycles
4. Push to git and await a build from Travis CI (which will publish a build - for now - to an S3
   bucket maintained by Edalex but which is publicly accessible and noted in build log)
5. Download resultant build, and commence testing (and any rework - do more RCs as needed)
6. Once agreed that code is ready for release, update root `build.sbt` so that `equellaStream` is
   `Stable` and commit
7. Now merge `release/2020.1.0` into `master` and push
8. Await build and then do a final validation of the resultant artefacts, if all in order tag the
   merge commit on master as `2020.1.0` - ensure to push the tag to git with `git push origin 2020.1.0`
9. Now go to GitHub to publish/create the new release utilising the pushed tag
10. Merge that tag point into develop (e.g. `git checkout develop && git pull && git merge 2020.1.0`) resolving any conflicts that may arise
11. Last step, update root `build.sbt` on `develop` to reflect the next planned feature release and
    setting `equellaStream` to `Alpha`

**Publishing release candidate builds:** It is possible to publish the individual RC builds on
GitHub too, just tag each point on the `release/` branch - e.g. with a tag of `2020.1.0-RC1`.

At this point a new stable release has been successfully released. However this means that the
immediately prior stable version needs to be made ready for any future possible hotfixes. To do
this, we create a branch now.

In the above example 2020.1.0 was the new version, so this would have replaced 2019.2.x. If the
latest (hotfix) release for 2019.2.x was 2019.2.3, then we'd want to branch off tag `2019.2.3` to
create a new branch called `stable-2019.2`. This is done via:

    git checkout master
    git pull
    git checkout -b stable-2019.2 2019.2.3
    git push -u origin stable-2019.2

_NOTE:_ Unfortunately here we're using `stable-<version>` rather than `stable/<version>` as
historically a `stable` branch was created for oEQ 6.5.

This newly created branch, will then be used below when creating hotfixes for 'previous' stable
releases.

### Hotfix Releases

Although there are essentially two processes here, they are the same other than the initial starting
point for the `hotfix/` branch and then the target for the merge (and later tag).

#### For current stable version

To do a hotfix for the current stable version (i.e. the most recent feature release) you use
`master` as the **base branch**.

#### For previous stable versions

To do a hotfix for one of the previous stable versions (i.e. versions which have been superseded
by a newer feature release currently sitting on `master`), you use `stable-<version>` as the **base
branch**. For example, if you wanted to do a new hotfix for 2019.1 your base branch would be
`stable-2019.1`.

#### Common Steps

With the above starting **base branches** the steps are as follows:

1. Checkout and pull the **base branch**
2. Create the new `hotfix/<version>` branch - .e.g `git checkout -b hotfix/2019.2.1`
3. Update root `build.sbt` to correct version information - refer to guidance above
4. Apply fixes that have already been made on other branches (ideally `develop`). Hopefully this can
   be simply achieved with `git cherry-pick`, otherwise do manually
5. Push and retrieve a Travis CI build to undertake testing
6. If all is in order, then merge `hotfix/<version>` branch back to **base branch**
7. Tag merge commit with version
8. Retrieve the build from the tag, and use to create release on GitHub
