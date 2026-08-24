-- edi.comanda definition

-- Drop table

-- DROP TABLE edi.comanda;

CREATE TABLE edi.comanda (
	codi int8 NOT NULL,
	"json" jsonb NULL,
	path_pdf varchar NULL,
	enviament_numero varchar NULL,
	missatge_tipus varchar NULL,
	missatge_numero varchar NULL,
	"data" date NULL,
	referencia varchar NULL,
	path_edi varchar NULL,
	"document" varchar NULL,
	observacions varchar NULL,
	usuari_logistica varchar NULL,
	codi_client_ames varchar NULL,
	nom_client_ames varchar NULL,
	deleted bool DEFAULT false NULL,
	inserted_at timestamptz NULL,
	updated_at timestamptz NULL,
	deleted_at timestamptz NULL,
	inserted_by varchar NULL,
	updated_by varchar NULL,
	deleted_by varchar NULL,
	updated_reason varchar NULL,
	deleted_reason varchar NULL,
	status varchar NULL,
	nad varchar NULL,
	bustia varchar NULL,
	client_profile jsonb NULL,
	codi_comanda_client varchar NULL,
	nad_path varchar NULL,
	CONSTRAINT comandaedi_pk UNIQUE (codi)
);

-- edi.linia definition

-- Drop table

-- DROP TABLE edi.linia;

CREATE TABLE edi.linia (
	codi int8 DEFAULT nextval('edi.seq_linia'::regclass) NOT NULL,
	codi_comanda int8 NOT NULL,
	quantitat numeric NOT NULL,
	codi_article varchar NOT NULL,
	codi_article_ames varchar NULL,
	observacions varchar NULL,
	data_inicial date NULL,
	data_final date NULL,
	tipus varchar NULL,
	status varchar NULL,
	inserted_at timestamptz NULL,
	updated_at timestamptz NULL,
	deleted_at timestamptz NULL,
	inserted_by varchar NULL,
	updated_by varchar NULL,
	deleted_by varchar NULL,
	updated_reason varchar NULL,
	deleted_reason varchar NULL,
	deleted bool NULL,
	ultim_albara varchar NULL,
	codi_linia_client varchar NULL,
	codi_comanda_client varchar NULL,
	codi_article_fab varchar NULL,
	acum_article numeric NULL,
	programa varchar NULL,
	CONSTRAINT linia_pk PRIMARY KEY (codi)
);


-- edi.linia foreign keys

ALTER TABLE edi.linia ADD CONSTRAINT linia_comandaedi_fk FOREIGN KEY (codi_comanda) REFERENCES edi.comanda(codi);