# Institution fixtures

The institutions the end-to-end tests run against. Each is a committed openEQUELLA institution export,
unpacked, so that changes to test data are reviewable in git like any other change.

```
institutions/
├── vanilla/                    A fixture
│   ├── institution/            The export itself - this is what gets imported
│   ├── details.txt             Optional: users, passwords, collections (4 fixtures have one)
│   └── coverageinclude.pattern Optional: coverage scoping (5 fixtures have one)
├── moodle/                     Also carries its own integration setup notes and scripts
└── importexport/               Not a fixture - see "The odd ones out" below
```

Inside `institution/` is one directory per *converter* - the server-side component that owns that slice
of an institution - plus `institutionInfo.xml` at the root:

```
institutionInfo.xml  acls/  itemdefinition/  items/  schema/  users/  workflow/  filestore/  ...
```

`items/` is bucketed: `items/<bucket>/<id>.xml` is an item's database row, and `items/<bucket>/<id>/`
beside it holds its attachments, its `_ITEM/` wizard metadata, and any generated thumbnails.

## Importing them

```bash
./sbt "project autotest" setupForTests            # all of them
./sbt "project autotest" "setupForTests vanilla"  # just one
```

This deletes and re-creates each institution, so never run it against anything you care about. The set
can also be narrowed from configuration with `tests.insts`.

## Changing one

Fixtures are edited by driving the application, not by hand-editing XML. The round trip is manual:

1. `./sbt "project autotest" "setupForTests vanilla"` - start from what is committed.
2. Make your change through the UI.
3. Export the institution from the server admin console, and download the `.tgz` it produces.
4. Unpack it **somewhere outside the repository**.
5. Copy into `institutions/vanilla/institution/` only the files your change actually touched.
6. `git diff autotest/institutions/vanilla`, then commit.

Step 5 is the one that matters, and the next section explains why. Unpacking the export *over* the
fixture is not an update - it will duplicate content and break the fixture.

### Why you copy across parts of an export, and not the export

An export is a serialisation of database rows, primary keys included, and **every import assigns new
keys**. `ItemConverter.importIt` saves each item, takes the id the database hands back, and records
old-to-new so that everything referring to that item can be repointed. So a re-export of an untouched
institution still differs from the fixture it came from:

- every `<id>` has changed, at every level of every file;
- every item's `dateForIndex` has been re-randomised;
- and because `items/` paths are built *from* the id, every item has moved to a new path.

Copying a whole export over a fixture therefore does not update it - it adds a second copy of
everything under new ids. That is not hypothetical. In May 2020 a small change to `vanilla` was made
that way: 625 files, 426 of them pure additions, leaving every item present twice. The fixture then
would not import, and it took a cleanup commit, a revert, and two more commits restoring items other
tests needed. The same change, redone by hand, was 51 files.

So work folder by folder, and copy only where you changed something. Most folders name their files
after a uuid or a name, which makes them straightforward: `users/`, `groups/`, `acls/`,
`itemdefinition/`, `schema/`, `mimetypes/`, `taxonomy2/` and the rest of the entity folders all keep
their paths across a round trip, so a differing file is a differing thing.

`items/` is the one to be careful with, along with `item_relations/`, `notifications/`, `favourites/`,
`keyresources/`, `keyset/` and the `qti*` folders. All of those name their files after the database id,
so on a re-export **every** file appears to be new. To find what genuinely changed, match items up by
the `<uuid>` and `<version>` inside the row XML rather than by path.

### Copying items in particular

An item is its row XML plus its data folder, and both have to travel together. Watch for one thing: a
handful of files elsewhere name items by database id rather than by uuid - `acls/entries.xml` (as
`I:<id>` and `D:<id>`), `item_relations/`, `favourites/` and the `qti*result/` folders. If a new item
is copied in without them, an ACL pointing at an id that is not in the tree is dropped **silently**,
while a relation pointing at one aborts the whole import.

Mixed generations of id across a fixture are fine - the importer re-keys everything regardless. What
breaks a fixture is the same uuid appearing twice.

## What always differs, and should be left alone

Never copy these across. Every one of them differs on every export, whatever you did or did not
change:

| | |
|---|---|
| `institutionInfo.xml` | Entirely environment-specific: the exporting server's URL, build version, admin password hash, and the institution's own id. Its three migration lists come from a `HashSet`, so they also reshuffle wholesale. Replacing it re-baselines the fixture, which stops it exercising the migrations it was built for. |
| `_THUMBS/` `_TILES/` `_VIDEOPREVIEW/` `_zips/` | Regenerated by the server. Only a fraction are committed, so a server that has run its thumbnail tasks produces hundreds of spurious files. |
| `auditlogs/` `auditlogs2/` | Logs, not content. Untick the audit logs box when exporting; only `dinuk` commits any. |
| `export.fmt` | Records a converter folder's on-disk format. Never edited. |

`acls/entries.xml` and `acls/expressions.xml` need the same suspicion for a subtler reason: they are
exported from unordered collections, so their order varies between exports, and an entry references
its expression by a database id that is itself reassigned on import. Expect both to differ every time
whether or not any permission changed, and read the diff rather than trusting it.

Two other things worth recognising. Content the *server* adds on import is not your change - a fresh
`keyset/RSA/<id>.json` keypair, for instance. And a fixture captured on an older release will show a
real, uniform difference where the schema has moved on since: re-exporting `vanilla`, captured on
2020.2, adds `<erroredIndexing>false</erroredIndexing>` to every item.

## The odd ones out

- **`importexport/`** is not a fixture. It holds whole archives, one per historical release
  (`v20251.tgz`, `50_institution.tgz`, `41_institution.tar.bz2`, …), which `ImportTest` imports to
  check that old exports still load. Adding one is a manual job: export from a server of that release
  and commit the archive as it comes.
- **`login/`** still carries the pre-modern `institutionData.xml` instead of `institutionInfo.xml`. A
  re-export writes the modern one, so do not read that as a file removed and another added.
- **`dinuk/`** is the only fixture committing audit logs.
- **`acl/`** and **`fiveo/`** are old enough to carry converter folders for features since removed
  (commerce, CAL). They import harmlessly - unknown folders are ignored.

## See also

- [../README.md](../README.md) - running the tests, configuration, and choosing a target instance.
- `ItemConverter`, `AclConverter` and `InstitutionImportServiceImpl` under
  `Source/Plugins/Core/com.equella.core/`, if you need to know exactly what a converter does with its
  folder.
