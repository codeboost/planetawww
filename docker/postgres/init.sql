CREATE ROLE pm LOGIN ENCRYPTED PASSWORD 'pm_password';
CREATE ROLE pm_admin LOGIN CREATEDB CREATEROLE ENCRYPTED PASSWORD 'pm_adm_password';

CREATE DATABASE pm_bands OWNER pm_admin;
CREATE DATABASE pm_band_template OWNER pm_admin;

UPDATE pg_database SET datistemplate=true WHERE datname='pm_band_template';

\c pm_band_template
CREATE EXTENSION ltree;
CREATE EXTENSION pg_trgm;
CREATE EXTENSION pgcrypto;

GRANT ALL ON ALL TABLES IN SCHEMA public TO pm_admin WITH GRANT OPTION;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pm_admin WITH GRANT OPTION;

\c pm_bands
CREATE EXTENSION pg_trgm;
