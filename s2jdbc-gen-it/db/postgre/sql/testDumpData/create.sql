create table ADDRESS (ID serial not null, CITY varchar(255), constraint ADDRESS_PK primary key(ID));
create table BIG_DECIMAL_ENTITY (BIG_DECIMAL_PROPERTY decimal(19,2));
create table BIG_INTEGER_ENTITY (BIG_INTEGER_PROPERTY bigint);
create table BOOLEAN_ENTITY (BOOLEAN_PROPERTY bool);
create table BYTE_ARRAY_ENTITY (BYTE_ARRAY_PROPERTY bytea, BLOB_PROPERTY oid);
create table BYTE_ENTITY (BYTE_PROPERTY smallint);
create table CALENDAR_ENTITY (DATE_PROPERTY date, TIME_PROPERTY time, TIMESTAMP_PROPERTY timestamp);
create table CHARACTER_ENTITY (CHARACTER_PROPERTY char(1));
create table DATE_ENTITY (DATE_PROPERTY date, TIME_PROPERTY time, TIMESTAMP_PROPERTY timestamp);
create table DEPARTMENT (ID serial not null, NAME varchar(255), constraint DEPARTMENT_PK primary key(ID));
create table DOUBLE_ENTITY (DOUBLE1_PROPERTY float8);
create table EMPLOYEE (ID serial not null, FIRST_NAME varchar(255) not null, LAST_NAME varchar(255) not null, DEPARTMENT_ID integer, ADDRESS_ID integer, constraint EMPLOYEE_PK primary key(ID));
create table ENUM_ENTITY (ENUM_PROPERTY integer);
create table FLOAT_ENTITY (FLOAT_PROPERTY float4);
create table INTEGER_ENTITY (INTEGER_PROPERTY integer);
create table LONG_ENTITY (LONG_PROPERTY bigint);
create table SERIALIZABLE_ENTITY (SERIALIZABLE_PROPERTY bytea, BLOB_PROPERTY oid);
create table SHORT_ENTITY (SHORT_PROPERTY smallint);
create table SQL_DATE_ENTITY (SQL_DATE_PROPERTY date);
create table STRING_ENTITY (STRING_PROPERTY varchar(255), CLOB_PROPERTY text);
create table TIME_ENTITY (TIME_PROPERTY time);
create table TIMESTAMP_ENTITY (TIMESTAMP_PROPERTY timestamp);

