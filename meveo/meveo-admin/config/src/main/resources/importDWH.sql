--CREATE TABLE "D_CUSTOMER_ACCOUNT"
--  (
--    "ID" NUMBER(19,0) NOT NULL ENABLE,
--    "CREATION_DATE" DATE NOT NULL ENABLE,
--    "TERMINATION_DATE" DATE,
--    "ZIPCODE" VARCHAR2(5 BYTE),
--    "STATUS"   VARCHAR2(255 BYTE) NOT NULL ENABLE,
--    "TYPE"    VARCHAR2(50 BYTE) NOT NULL ENABLE,
--    PRIMARY KEY ("ID")
--  )
-- 
/*
 CREATE TABLE MEVEODWH.D_CUSTOMER_ACCOUNT 
(
  ID NUMBER(19, 0) NOT NULL 
, CREATION_DATE DATE NOT NULL 
, TERMINATION_DATE DATE 
, ZIPCODE VARCHAR2(5) 
, STATUS VARCHAR2(255) NOT NULL 
, TYPE VARCHAR2(50) NOT NULL 
, CONSTRAINT D_CUSTOMER_ACCOUNT_PK PRIMARY KEY 
  (
    ID 
  )
  ENABLE 
);
*/