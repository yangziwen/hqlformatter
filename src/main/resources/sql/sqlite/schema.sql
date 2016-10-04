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

-- request_mapping_info
DROP TABLE IF EXISTS "request_mapping_info";
CREATE TABLE "request_mapping_info" (
	id integer PRIMARY KEY AUTOINCREMENT,
	request_url varchar(50) NOT NULL,
	project varchar(50) NOT NULL,
	class_name varchar(50) NOT NULL,
	method_name varchar(50) NOT NULL,
	return_type varchar(50) NOT NULL
);