-- table_info
DROP TABLE IF EXISTS "table_info";
CREATE TABLE "table_info" (
	id integer PRIMARY KEY AUTOINCREMENT,
	database varchar(50) NOT NULL,
	table_name varchar(50) NOT NULL,
	description varchar(50)
);
CREATE UNIQUE INDEX "table_name_database_idx" ON "table_info" ("table_name", "database");

-- table_relation
DROP TABLE IF EXISTS "table_relation";
CREATE TABLE "table_relation" (
	id integer PRIMARY KEY AUTOINCREMENT,
	table_id integer NOT NULL,
	dependent_table_id integer NOT NULL
);