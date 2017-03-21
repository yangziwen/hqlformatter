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

-- style_report
DROP TABLE IF EXISTS "style_report";
CREATE TABLE "style_report" (
	id integer PRIMARY KEY AUTOINCREMENT,
	file_path varchar(255) NOT NULL,
	file_name varchar(50) NOT NULL,
	line_number integer NOT NULL,
	line_content varchar(255) NOT NULL,
	capture varchar(50) NOT NULL,
	suggest varchar(255)
);