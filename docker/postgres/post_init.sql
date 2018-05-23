\c pm_bands
GRANT SELECT ON bands TO pm;

-- TODO: use admin to do the following
\c pm_bands
CREATE DATABASE planetamoldova_net WITH TEMPLATE pm_band_template;
ALTER DATABASE planetamoldova_net OWNER TO pm_admin;

INSERT INTO bands (band_name, email_dom, database_name, database_user, database_pwd) VALUES ('Planeta Moldova', 'planetamoldova.net', 'planetamoldova_net', 'planetamoldova_net', 'm59D_Ngt_gAS6Nr7');

CREATE ROLE planetamoldova_net NOINHERIT LOGIN ENCRYPTED PASSWORD 'm59D_Ngt_gAS6Nr7';

\c planetamoldova_net
GRANT ALL ON ALL TABLES IN SCHEMA public TO planetamoldova_net;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO planetamoldova_net;

-- TODO: Create default user
-- INSERT INTO users ...;

-- Test DB
\c pm_bands
CREATE DATABASE pm_test_com WITH TEMPLATE pm_band_template;
ALTER DATABASE pm_test_com OWNER TO pm_admin;

INSERT INTO bands (band_name, email_dom, database_name, database_user, database_pwd) VALUES ('Test', 'pm_test.com', 'pm_test_com', 'pm_test_com', 'test-password');

CREATE ROLE pm_test_com NOINHERIT LOGIN ENCRYPTED PASSWORD 'test-password';

\c pm_test_com
GRANT ALL ON ALL TABLES IN SCHEMA public TO pm_test_com;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pm_test_com;
