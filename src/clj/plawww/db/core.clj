(ns plawww.db.core
  (:require
   [clojure.java.jdbc :as j]
   [clojure.tools.logging :refer [debug error]]
   [com.stuartsierra.component :as component]
   [environ.core :refer [env]]
   [honeysql.core :as hsql]
   [plawww.db.postgresql]
   [plawww.db.hikari-pool :as hikari-pool]))

(def queue-hints-by-default (atom true))
(def db-core {:classname   "org.postgresql.Driver"
              :subprotocol "postgresql"})

(defonce db-server-url (atom "//localhost:9999"))
(defonce band-conn-cache (atom {}))
(defonce service-db (atom nil))

(defn- subname
  "Return a database url given a database name and database server URL.
   By default, uses db-server-url for the database server."
  ([db-name db-server]
   (str db-server \/ db-name))
  ([db-name]
   (subname db-name @db-server-url)))

(defn- service-spec
  "Create a db-spec from a username and password, and an optional database server.
   By default, uses db-server-url for the database server, and uses
   `p_bands` for the default database name."
  ([username pwd db-name db-server]
   (assoc db-core :user username :password pwd :subname (subname db-name db-server)))
  ([username pwd db-name]
   (service-spec username pwd db-name @db-server-url))
  ([username pwd] (service-spec username pwd "pm_bands")))


(defn set-server!
  "Set the server url."
  [s]
  (reset! db-server-url s))

(defn connect!
  "Connect to the service database with a given username and password and save the connection."
  ([username pwd]
   (connect! username pwd "pm_bands"))
  ([username pwd db-name]
   (reset! service-db (service-spec username pwd db-name))))

(def ^:private application-name (atom "PLAWWW"))

(defn set-application-name!
  "Set the application name. Intended to be used to track database connections."
  [app-name]
  (reset! application-name app-name))

(defn create-band-db
  "Create a db spec where instead of a connection pool, we have a standard jdbc db-spec."
  [{:keys [database_name database_pwd database_user band_id band_name
           database_server]}]
  (assoc db-core
    :user          database_user
    :password      database_pwd
    :subname       (subname database_name (or database_server @db-server-url))
    :fid           band_id
    :band_name     band_name
    :database_name database_name
    :ApplicationName @application-name))

(defn- maybe-query
  "Returns nil if fails to connect."
  [db q]
  (try (j/query db q) (catch Exception e nil)))

(defn- lookup-band-connection
  "Lookup a band in the database and return a connection object if found."
  [id]
  (if-let [band-results (j/query @service-db ["SELECT * FROM bands WHERE band_id=?" id])]
    (if-let [band-data (first band-results)]
      (if-let [n (:max-pool-size env)]
        (hikari-pool/pool (create-band-db band-data) n)
        (create-band-db band-data))
      (throw (Exception. (str "no band " id))))
    (do
      (error "couldn't connect to database at " (:subname @service-db) " trying again in 10 seconds")
      (Thread/sleep 10000)
      (recur id))))

(def lookup-lock (Object.))

(defn find-band-connection
  "Try to find a band in the bands database. If found, create a connection and cache it."
  [id]
  (locking lookup-lock
    (or (get @band-conn-cache id)
        (if-let [conn (lookup-band-connection id)]
          (do
            (swap! band-conn-cache assoc id conn)
            conn)))))

(defn get-band-connection
  "Get a band connection."
  [id]
  (or (get @band-conn-cache id) (find-band-connection id)))

(defn set-queue-hints!
  "If set to true, hints are queued by default."
  [v]
  (reset! queue-hints-by-default v))

(defn- band-connection
  "Return a connection map. TODO: make this always take a number. Need
  to get rid of deprecated functions below to make this happen."
  [fdb]
  (if (number? fdb)
    (assoc (get-band-connection fdb) :fid fdb)
    (if-not (:fid fdb)
      (throw (Exception. (str "cannot make band connection from" fdb)))
      fdb)))

(defn band-db
  "Return a band database map usable in jdbc or the views system. Optionally
   takes a second boolean argument indicating whether hints should be queued
   or sent immediately."
  ([fid] (band-db fid @queue-hints-by-default))
  ([fid queue-hints?] (assoc (band-connection fid) :queue-hints? queue-hints?)))


;----------------------------------------------------------

(defrecord Database [opts]
  component/Lifecycle
  (start [this]
    (println "Starting Database.")
    (when (nil? @service-db)
      (let [{:keys [server user password app-name]} opts]
        (set-application-name! app-name)
        (set-server! server)
        (connect! user password)))
    this)

  (stop [this]
    (dissoc this :db)
    this))


(defn new-database
  "Creates a Database component. Must use `component/start` to actually connect to the database.
  The following keys are required in `options`:
    :app-name  - string. Application name, eg. 'planeta-md'.
    :server    - string. Database host:port, eg. '//localhost:5432'.
    :bands-db  - string. Service database name, eg. 'pm_bands'.
    :user      - string. User connecting to the database.
    :password  - string. Database password for the user `user`
  "
  [options]
  (Database. options))