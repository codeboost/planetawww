(ns plawww.db.hikari-pool
  "Support for Hikari connection pool."
  (:import
   [java.util Properties]
   [com.zaxxer.hikari HikariConfig HikariDataSource])
  (:require
   [camel-snake-kebab.core :refer [->kebab-case]]
   [environ.core :refer [env]]))

(def hikari-properties
  "Valid configuration properties. Must match properties in
  https://github.com/brettwooldridge/HikariCP/wiki/Configuration"
  ["connectionTimeout"
   "idleTimeout"
   "maxLifetime"
   "minimumIdle"
   "poolName"
   "initializationFailFast"
   "isolateInternalQueries"
   "allowPoolSuspension"
   "readOnly"
   "registerMbeans"
   "connectionInitSql"
   "validationTimeout"
   "leakDetectionThreshold"])

(defn set-env-properties!
  "Tries to set Hikari properties from environment variables. For each property
  it looks for an environment variable of the form HIKARI_PROPERTY_NAME where
  PROPERTY_NAME is the Hikary property and sets it if it exists.
  (e.g. connectionTimeout is HIKARI_CONNECTION_TIMEOUT)"
  [^Properties props properties]
  (doseq [k properties]
    (when-let [v (get env (keyword (str "hikari-" (->kebab-case k))))]
      (.setProperty props k v))))

(defn pool
  "Create a new PostgreSQL Hikari connection pool of of maximum size n
  for the given db-spec."
  [db-spec n]
  (let [[_ server port db] (re-matches #"//([^/:]+)(:\d+)?/(.+)" (:subname db-spec))
        props (doto (Properties.)
                (.setProperty "dataSourceClassName" "org.postgresql.ds.PGSimpleDataSource")
                (.setProperty "dataSource.serverName" server)
                (.setProperty "dataSource.portNumber" (if port (subs port 1) "5432"))
                (.setProperty "dataSource.databaseName" db)
                (.setProperty "dataSource.user" (:user db-spec))
                (.setProperty "dataSource.ApplicationName" (or (:ApplicationName db-spec) "pm-untitled"))
                (.setProperty "dataSource.password" (:password db-spec))
                (.setProperty "maximumPoolSize" (str n))
                (set-env-properties! hikari-properties))]
    {:datasource (HikariDataSource. (HikariConfig. props))}))

(defn close!
  "Close a connection pool."
  [^HikariDataSource ds]
  (.close ds))