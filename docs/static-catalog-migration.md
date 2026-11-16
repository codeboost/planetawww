# Plan: Static JSON catalog — removing MySQL + Mediadrop from the website

## Goal

The website (`planeta-crt`) stops reading the catalog from MySQL. Instead it reads a
static `catalog.json` file at startup/request time. MySQL and Mediadrop are removed
from the deployment entirely. The client (ClojureScript app) is not touched at all.

## Why this is safe

- The only DB surface is `db/get-media` and `db/get-categories`, called from
  `load-db-data` in `src/clj/plawww/handler.clj`. The result is serialized to JSON and
  inlined into `<head>` as `var kolbasulPlanetar = {...}` on every page request.
- There is no write path from the site, no per-request variation, no user data.
- `get-comments` is dead code (no route, no UI). `views` is selected but never used
  by the client.
- Snapshot sizing (Feb-2024 dump): 240 media items, 95 tags, 7 categories →
  ~60–90 KB JSON, ~20 KB gzipped. Every visitor already downloads this inline today.

## Contract: the JSON shape must be byte-compatible

The static file must reproduce exactly what `load-db-data` + `json/write-str` emit
today. Specifically:

1. **Top-level shape**: `{"media": [...], "categories": [...]}`.
2. **Nil keys are omitted, not null.** `massage-item` (`db/core.clj`) strips nil
   values from each item; client code relies on key absence
   (e.g. `(when categories ...)` in `mediadb/core.cljs`).
3. **`publish_on` is a `"YYYY-MM-DD"` string** (from `java.sql.Date` `toString`),
   parsed client-side with `(js/Date. s)`. Do not change the format.
4. **`tags`**: vector of tag *name strings* (trimmed). Tag names appear in
   shareable URLs (`?taguit=...`). Migrate existing dirt (historic leading spaces
   etc.) as-is; clean up later as a separate, deliberate change.
5. **`categories` on an item**: vector of category *ids* (integers).
6. **Category objects**: `{id, name, slug}`, ordered by name.
7. **One file per media item** (1:1 in real data even though the schema allows 1:N).
   `filename` == `unique_id` for local storage. `storage_id` 1 = local file,
   3 = YouTube (`unique_id` is then the YouTube video id).

## Phase 0 — Export the canonical snapshot (on the server, DB still running)

1. Verify whether any live items use `storage_id = 3` (YouTube). The local
   `mediadrop.sql` seed has none, but the live volume (`planeta-data/mysql`) is newer.
2. Generate `catalog.json` **using the existing code path** so the shape is identical
   by construction. From a REPL against the live DB:

   ```clojure
   (require '[plawww.handler :as h] '[clojure.data.json :as json])
   (spit "catalog.json" (json/write-str (h/load-db-data)))
   ```

3. Sanity check: `curl` the live site, extract the inlined `kolbasulPlanetar` JSON
   from the HTML, and diff against the file. Ship only when identical.

## Phase 1 — Website reads the file instead of MySQL

Changes in `planetawww`:

- `handler.clj`: `load-db-data` becomes ~"read + parse `catalog.json`". Keep the
  inline `<head>` injection — do NOT switch the client to fetching; that would force
  an async init/loading state for zero gain at ~20 KB gzipped.
  - File location: inside the media data volume (`(media-path "/catalog.json")` →
    `../planeta-data/mediadrop/catalog.json` in dev, `/app/data/catalog.json` in
    prod). Rationale: the catalog describes exactly that content and changes on the
    same cadence; content updates become `git pull` + restart with history/diff/
    rollback, no image rebuild.
  - Read strategy: `slurp` + parse per request is fine at this size (matches today's
    behavior of querying per request, and picks up catalog changes without restart).
    Optionally cache on mtime later.
- `server.clj`: drop the `PLANETA_MYSQL_PWD` boot check; instead fail fast if
  `catalog.json` is missing/unparseable.
- Delete `src/clj/plawww/db/core.clj`.
- `project.clj`: remove `mysql/mysql-connector-java`, `honeysql`,
  `org.clojure/java.jdbc`.

Verification: run locally against the exported `catalog.json`, diff rendered HTML
`<head>` payload against production. Deploy with the normal `deploy.sh` flow. The
old image + compose file remain the rollback path.

## Phase 2 — Deployment cleanup (`planeta-docker`)

Once Phase 1 is verified in production:

- `docker-compose.yml`: remove `mysql` and `planeta-mediadrop` services; remove
  `links`/`depends_on` and `PLANETA_MYSQL_PWD` from `planeta-crt`.
- `nginx/planeta.conf`: remove the `admin.planetamoldova.net` server block,
  htpasswd, and the 2 GB `client_max_body_size`.
- Let the `admin.planetamoldova.net` certificate lapse (or keep renewing if the
  subdomain gets reused for the new publishing app later — decide in the PRD).
- `start.sh` / README: drop the MySQL password requirement.
- Keep the final `mysqldump` of the live DB archived in `planeta-data` (it is the
  provenance of the catalog and the safety net).

## Explicitly out of scope (later, optional)

- Replacing the Clojure server with pure nginx static hosting (needs porting of the
  range-request middleware and SPA fallback). The JVM container is cheap; don't rush.
- Cleaning tag names / URL schema changes.
- The catalog publishing app (Mediadrop replacement) — separate PRD.

## Field inventory (what the client actually uses)

Kept because used: `id`, `type`, `slug`, `title`, `subtitle`*, `description`,
`description_plain`, `duration`, `publish_on`, `tags`, `categories`, `filename`,
`unique_id`, `storage_id`, `size`, `width`, `height`.
Dead on arrival (emit for byte-compat now, candidates for removal later): `views`,
`media_id`.
(*`subtitle` currently always nil in the seed data → omitted by nil-stripping.)
