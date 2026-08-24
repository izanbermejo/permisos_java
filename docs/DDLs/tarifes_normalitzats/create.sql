-- tarifes_normalitzats."preuOLD" definition

-- Drop table

-- DROP TABLE tarifes_normalitzats."preuOLD";

CREATE TABLE tarifes_normalitzats."preuOLD" (
	tarifa varchar NULL,
	artint varchar NULL,
	pr01 numeric(11, 3) NULL,
	pr02 numeric(11, 3) NULL,
	pr03 numeric(11, 3) NULL,
	pr04 numeric(11, 3) NULL,
	pr05 numeric(11, 3) NULL,
	pr06 numeric(11, 3) NULL,
	pr07 numeric(11, 3) NULL,
	pr08 numeric(11, 3) NULL,
	pr09 numeric(11, 3) NULL,
	pr10 numeric(11, 3) NULL,
	pr11 numeric(11, 3) NULL,
	pr12 numeric(11, 3) NULL,
	aclfab varchar NULL,
	aclref varchar NULL,
	unsbos varchar NULL,
	unscai varchar NULL
);


-- tarifes_normalitzats."tarifaOLD" definition

-- Drop table

-- DROP TABLE tarifes_normalitzats."tarifaOLD";

CREATE TABLE tarifes_normalitzats."tarifaOLD" (
	tarifa varchar NULL,
	divisa varchar NOT NULL,
	inserted_at timestamptz NULL,
	updated_at timestamptz NULL,
	inserted_by varchar NULL,
	updated_by varchar NULL,
	enabled bool DEFAULT false NULL,
	status varchar DEFAULT 'OPEN'::character varying NULL,
	updated_reason varchar NULL,
	deleted bool DEFAULT false NULL,
	deleted_by varchar NULL,
	deleted_at timestamptz NULL,
	deleted_reason varchar NULL,
	vinculada varchar NULL,
	CONSTRAINT tarifa_unique UNIQUE (tarifa)
);


-- tarifes_normalitzats.tarifa definition

-- Drop table

-- DROP TABLE tarifes_normalitzats.tarifa;

CREATE TABLE tarifes_normalitzats.tarifa (
	codi int8 NOT NULL,
	nom varchar NULL,
	divisa varchar NOT NULL,
	inserted_at timestamptz NULL,
	updated_at timestamptz NULL,
	inserted_by varchar NULL,
	updated_by varchar NULL,
	enabled bool DEFAULT false NULL,
	status varchar DEFAULT 'OPEN'::character varying NULL,
	updated_reason varchar NULL,
	deleted bool DEFAULT false NULL,
	deleted_by varchar NULL,
	deleted_at timestamptz NULL,
	deleted_reason varchar NULL,
	vinculada int8 NULL,
	observacions varchar NULL,
	tram1 int8 NULL,
	tram2 int8 NULL,
	tram3 int8 NULL,
	tram4 int8 NULL,
	tram5 int8 NULL,
	tram6 int8 NULL,
	tram7 int8 NULL,
	tram8 int8 NULL,
	tram9 int8 NULL,
	tram10 int8 NULL,
	tram11 int8 NULL,
	tram12 int8 NULL,
	CONSTRAINT tarifa_pk PRIMARY KEY (codi),
	CONSTRAINT tarifa_tarifa_fk FOREIGN KEY (vinculada) REFERENCES tarifes_normalitzats.tarifa(codi) ON DELETE SET NULL
);


-- tarifes_normalitzats.preu definition

-- Drop table

-- DROP TABLE tarifes_normalitzats.preu;

CREATE TABLE tarifes_normalitzats.preu (
	codi int8 DEFAULT nextval('tarifes_normalitzats.seq_preu'::regclass) NOT NULL,
	artint varchar NULL,
	pr01 numeric(11, 3) NULL,
	pr02 numeric(11, 3) NULL,
	pr03 numeric(11, 3) NULL,
	pr04 numeric(11, 3) NULL,
	pr05 numeric(11, 3) NULL,
	pr06 numeric(11, 3) NULL,
	pr07 numeric(11, 3) NULL,
	pr08 numeric(11, 3) NULL,
	pr09 numeric(11, 3) NULL,
	pr10 numeric(11, 3) NULL,
	pr11 numeric(11, 3) NULL,
	pr12 numeric(11, 3) NULL,
	aclfab varchar NULL,
	aclref varchar NULL,
	unsbos varchar NULL,
	unscai varchar NULL,
	codi_tarifa int8 NOT NULL,
	CONSTRAINT preu_pk PRIMARY KEY (codi)
);

-- tarifes_normalitzats.preu foreign keys

ALTER TABLE tarifes_normalitzats.preu ADD CONSTRAINT preu_tarifa_fk FOREIGN KEY (codi_tarifa) REFERENCES tarifes_normalitzats.tarifa(codi) ON DELETE CASCADE ON UPDATE CASCADE;