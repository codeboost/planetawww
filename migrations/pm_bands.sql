\c pm_bands

CREATE TABLE bands (
    band_id integer NOT NULL,
    band_name character varying(300) NOT NULL,
    email_dom character varying(200) NOT NULL,
    database_name character varying(200),
    database_user character varying(100),
    database_pwd character varying(400)
);

CREATE SEQUENCE bands_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE bands_id_seq OWNED BY bands.band_id;

ALTER TABLE ONLY bands ALTER COLUMN band_id SET DEFAULT nextval('bands_id_seq'::regclass);

ALTER TABLE ONLY bands ADD CONSTRAINT email_domain UNIQUE (email_dom);
ALTER TABLE ONLY bands ADD CONSTRAINT band_id PRIMARY KEY (band_id);
