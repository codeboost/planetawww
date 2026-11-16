# PRD: Planeta Catalog Publisher (Mediadrop replacement)

Status: Draft v1 — 2026-08-11
Depends on: `docs/static-catalog-migration.md` (the website reads `catalog.json`
from the `planeta-data` volume; MySQL and Mediadrop are removed from deployment).

## 1. Summary

A single-user, local TypeScript/Node web app that replaces Mediadrop as the way
content gets onto planetamoldova.net. It owns the entire content pipeline short of
shipping: ingest a media file, probe its technical metadata, generate thumbnails,
edit catalog metadata, manage categories, and write everything into a local
checkout of the `planeta-data` repository. Shipping stays manual and deliberate:
the operator reviews with `git diff` and pushes; the server pulls.

## 2. Background

The website is a ClojureScript SPA that receives the full catalog as one inlined
JSON object (`kolbasulPlanetar`). Historically that JSON was built per-request
from a MySQL database maintained by Mediadrop (CentOS 7 / Python 2 — long EOL,
effectively unrebuildable). After the static-catalog migration, the JSON lives in
`planeta-data` as `catalog.json` and Mediadrop's only remaining role — the ingest
pipeline and the editing UI — is unfilled. This app fills it.

Catalog scale: ~240 items, ~95 tags, 7 categories, a few updates per year.
Every design choice below favors simplicity over throughput.

## 3. Goals / Non-goals

### Goals
- Add a new audio, video, or YouTube item to the catalog in under a minute of
  operator effort, with all technical metadata derived automatically.
- Edit any item's catalog metadata safely (schema-validated output).
- Manage categories, including their slug-keyed images.
- Produce byte-compatible `catalog.json` — the website client is never touched.
- Leave every change sitting in the `planeta-data` working tree for review;
  never touch git, never touch the network (except YouTube oEmbed, see 6.4).

### Non-goals (v1)
- Publishing/deploy (git commit, push, ssh, server pull) — operator's job.
- Multi-user, auth, hosting — the app binds to localhost, one operator.
- Drafts/scheduling — everything written to `catalog.json` is live-on-next-push;
  the un-pushed working tree is the draft zone.
- Tag rename/merge tools, bulk item operations, integrity checker (see §10).
- In-app site preview — the real website runs locally against the same
  `planeta-data` checkout (`PLANETA_MEDIADROP_DATA`) and is the canonical preview.
- Transcoding. Files are ingested as-is; encoding happens before the app.
- Comments, views, likes — dead Mediadrop features, not carried over.

## 4. Operating environment

- Runs locally: `npm start` → web UI on `http://localhost:<port>` (localhost only).
- Configuration: path to the `planeta-data` checkout (env var or config file;
  default `../planeta-data` matching the historical repo layout).
- External tools (must be on PATH; app fails fast at startup with a clear message
  if missing): `ffprobe` and `ffmpeg` (probing, video frame extraction, and image
  resizing — ffmpeg covers thumbnails too, no ImageMagick dependency needed).
- The app never modifies anything outside the configured `planeta-data` directory.

### Directory contract (inside `planeta-data/mediadrop/`, served as `/data` by the site)

```
catalog.json                     ← single source of truth
media/<filename>                 ← media files; filename == unique_id
images/media/<id>s.jpg           ← small thumbnail (video items)
images/media/<id>l.jpg           ← large thumbnail (video items)
images/categories/<slug>.jpg     ← one image per category
carti/...                        ← books/PDFs; out of scope, never touched
```

## 5. The catalog.json contract (normative)

The app reads and writes `catalog.json` directly in the exact legacy shape. This
is the compatibility boundary; violating it breaks the live site.

1. Top level: `{"media": [...], "categories": [...]}`.
2. **Absent, not null**: a field with no value is omitted from the object
   entirely. The client tests key presence (e.g. `(when categories ...)`).
3. `publish_on`: string `"YYYY-MM-DD"`. Parsed client-side via `new Date(s)`.
4. `tags`: array of trimmed tag-name strings. Tag names appear in shareable URLs
   (`?taguit=`). Existing historic values (even ugly ones) are preserved as-is.
5. `categories` (on an item): array of integer category ids.
6. Category object: `{id: int, name: string, slug: string}`; the array is ordered
   by name.
7. Item fields (all that the legacy pipeline emitted, including ones the client
   ignores — emitted for byte-compat): `id`, `type` (`"audio"` | `"video"`),
   `slug`, `publish_on`, `title`, `subtitle`, `description`,
   `description_plain`, `duration` (seconds, int), `views`, `media_id`, `tags`,
   `unique_id`, `filename`, `size` (bytes), `width`, `height`,
   `storage_id`, `categories`.
8. `storage_id`: `1` = local file (then `filename == unique_id`, and the site
   serves `/data/media/<filename>`), `3` = YouTube (then `unique_id` is the
   YouTube video id and `filename`/`size`/`width`/`height` follow whatever the
   live export shows for such items — see Open Questions).
9. `id` is a positive integer, unique across the catalog. New id = max(id) + 1.
   Ids are never reused (thumbnails and shareable item URLs are keyed on id).
10. `slug` is unique across items.

**Validation**: every write of `catalog.json` passes through a schema validator
(e.g. zod) enforcing all of the above. A write that fails validation is aborted
loudly; the app must be unable to persist an invalid catalog. On startup the app
validates the existing file and reports (but still displays) violations, since
the initial file comes from the MySQL export and is definitionally correct.

**Formatting**: the file is written deterministically (stable key order, stable
item order — preserve existing array order; new items append) so that
`git diff` shows only real changes.

## 6. Features

### 6.1 Catalog browser (home screen)
- List all items: title, type, id, publish date, tags, categories; sortable and
  filterable by text/tag/category/type.
- Entry points: add item (file drop / YouTube URL), edit item, manage categories.

### 6.2 Ingest — local file (the core flow)
1. Operator drops (or file-picks) an audio/video file.
2. App computes: next `id`; slug from title-guess (filename cleaned up);
   `unique_id`/`filename` per existing convention `{id}-{slug}.{ext}` (e.g.
   `53-10lei.mp3`); `type` from probe.
3. App shells `ffprobe`: `duration` (rounded to int seconds), `size` (bytes),
   and for video `width`/`height` (absent for audio, matching legacy data).
4. Video only: app extracts a representative frame (default: 25% into the
   file) via `ffmpeg` and writes `<id>s.jpg` and `<id>l.jpg` at the dimensions
   of the existing thumbnails in `planeta-data` (measured at implementation
   time — see Open Questions). UI offers: pick a different timestamp
   (scrubber or seconds field, regenerates) or upload a custom image (resized
   to both sizes). Audio items get no thumbnails (site uses its default image).
5. Prefilled item form opens: title (from filename, editable — editing title
   before first save regenerates slug/filename), subtitle, description,
   description_plain, tags (autocomplete against all existing tags; new tags
   allowed; input trimmed), categories (checkboxes), publish_on (default:
   today).
6. On save: media file is copied into `media/`, thumbnails land in
   `images/media/`, item is appended to `catalog.json`, file is validated and
   written. Nothing is written before save; cancel leaves no trace.
7. Collisions (id, slug, filename already present) are hard errors surfaced in
   the form, not auto-resolved silently.

### 6.3 Edit / delete item
- Edit any metadata field of an existing item. `filename`/`unique_id`/`size`/
  `width`/`height`/`duration` are read-only in the form (they describe the
  file; replacing the file is done by re-probing — an explicit "replace media
  file" action that keeps id and filename-derived name but re-runs the probe).
- Changing a slug or title of an already-published item warns that shareable
  URLs may break (item URLs use title; `?taguit=` uses tag names).
- Regenerate/replace thumbnails for video items at any time.
- Delete item: removes the catalog entry and offers (checkbox, default on) to
  delete the media file and thumbnails from the working tree. Git history is
  the undo story; the app itself needs no soft-delete.

### 6.4 Ingest — YouTube item
- Operator pastes a YouTube URL; app extracts the video id → `unique_id`,
  `storage_id: 3`.
- Metadata prefill via YouTube oEmbed (title) when the network allows; duration
  entered manually (oEmbed does not provide it). Everything else as in 6.2.
- No file copy, no probing, no thumbnails required by the site for YouTube
  items unless the live export shows otherwise (Open Questions).

### 6.5 Category management
- List categories with id, name, slug, image thumbnail, and item count.
- Create category: name → suggested slug (editable before first save); id =
  max+1; requires an image (any size; app resizes/crops to match existing
  category image dimensions) written to `images/categories/<slug>.jpg`.
- Edit name freely. Editing slug carries a prominent warning (breaks
  `?colectia=` URLs and renames the image file) and performs the image rename.
- Replace image at any time.
- Delete category only when no item references it; otherwise show the blocking
  items.

### 6.6 Ship-it panel (informational only)
- A persistent, read-only indicator of the `planeta-data` working tree state:
  dirty/clean and the list of changed paths (via `git status --porcelain`,
  read-only — the app never runs mutating git commands).
- Shows the operator's next steps as copy-paste commands
  (`git add -A && git commit ... && git push`, then on the server
  `cd planeta-data && git pull`).
- Reminder link to the local site preview workflow.

## 7. UX principles

- One screen per job: browser, item form, category manager. No dashboard-ware.
- Everything destructive or URL-breaking gets an explicit inline warning at the
  point of action, not a generic confirm dialog.
- The app is honest about its boundary: it edits a working tree. The words
  "publish"/"live" never appear on save buttons — saving writes files locally;
  the ship-it panel owns the story of getting them live.

## 8. Technical notes

- Stack: TypeScript/Node backend serving a web UI (implementer's choice of
  Vite/React or server-rendered — not specced here); no database, no daemon
  state. The filesystem is the state; every screen re-reads from disk so
  external edits (hand-editing `catalog.json`, git operations) are picked up
  on refresh without restart.
- All shell-outs (`ffprobe`, `ffmpeg`) run with explicit argument arrays (no
  shell interpolation of filenames).
- Writes are atomic: write to temp file in the same directory, validate, rename
  over the target.
- The app should tolerate `catalog.json` fields it doesn't know about
  (round-trip unknown keys untouched) so a future schema addition doesn't
  require an app release first.

## 9. Bootstrap / migration

- The initial `catalog.json` is produced by the Phase 0 export in
  `docs/static-catalog-migration.md` (generated by the existing Clojure code
  path against the live DB, then diff-verified against production HTML).
- The app performs no migration itself; it opens whatever valid catalog it
  finds. First run against the exported file must be read-only-clean: open the
  app, touch nothing, confirm `git status` shows no changes.
- Keep the final `mysqldump` archived in `planeta-data` as provenance.

## 10. Future work (explicitly deferred from v1)

- **Integrity checker**: report of catalog↔disk mismatches (missing media
  files, orphaned files, missing thumbnails, categories without images).
  Deferred by decision; noted here because without a database there are no
  foreign keys, and a typo'd filename ships silently and 404s in production.
  Revisit after the first few real publishes.
- Tag rename/merge tools (the deliberate cleanup path for historic tag dirt).
- Bulk item operations (multi-select tag/category application).
- One-button publish (app-driven git commit/push + server pull).
- Client-side schema cleanup (drop `views`/`media_id`, de-dupe
  `filename`/`unique_id`) — requires touching the website, coordinate with a
  site release.

## 11. Open questions (need the live server; resolve before implementation)

1. **Video and YouTube reality check**: the local Feb-2024 seed dump contains
   240 items, all audio, all `storage_id = 1`. The live DB (`planeta-data/mysql`
  volume) is newer. Before building the video/YouTube paths, inspect the live
  export: are there video items? YouTube items? What fields do they carry
  (`width`/`height`/`filename` for storage_id 3)?
2. **Thumbnail dimensions**: measure existing `<id>s.jpg`/`<id>l.jpg` and
   category images in `planeta-data` and pin those dimensions in the app config.
3. **oEmbed dependency**: confirm YouTube oEmbed is acceptable as the only
   network call (it is optional — the form works with manual entry when offline).

## 12. Acceptance (v1 is done when)

1. Open the app against a fresh clone of `planeta-data` + exported
   `catalog.json`; browse all items; `git status` stays clean.
2. Ingest a new audio file end-to-end: drop → prefilled form → save →
   `git diff` shows exactly one new catalog entry (valid, appended, stable
   formatting) and one new file in `media/` with the `{id}-{slug}.{ext}` name.
3. Ingest a video file: same, plus two correctly-sized thumbnails; replace the
   auto frame with a custom image and see the files update.
4. Add a YouTube item by URL; entry validates with `storage_id: 3`.
5. Edit an existing item's tags/categories/description; diff shows only those
   lines changed.
6. Create a category with an image; assign an item to it; the local site
   renders the category page with the image.
7. Attempt to save an item with a duplicate slug and with a missing title →
   both blocked with inline errors; `catalog.json` on disk never invalid at
   any point (kill -9 during save leaves either old or new file, never a
   partial one).
8. Run the real site locally against the modified checkout and verify the new
   items play.
