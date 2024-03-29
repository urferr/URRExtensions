SELECT b.benutzer_id, b.benutzer_nr, o.ORGEINHEIT_NR FROM BENUTZER b, ORGEINHEIT o 
WHERE o.BENUTZER_ID = b.BENUTZER_ID AND o.INAKTIV_DAT IS NOT null AND b.BENUTZER_NR LIKE 'PD_%';

ALTER TABLE BENUTZER
	 DISABLE CHECK (SYSBENUTZERSTATUS_ID IS NOT NULL), 
	 DISABLE CHECK (LOGINVERSUCHE IS NOT NULL), 
	 DISABLE CHECK ("BENUTZER_NR" IS NOT NULL);

ALTER TABLE BENUTZER ENABLE CONSTRAINT SYS_C002083295;
ALTER TABLE BENUTZER DISABLE CONSTRAINT SYS_C002086316;
ALTER TABLE BENUTZER DISABLE CONSTRAINT SYS_C002086317;

Execute IMMEDIATE SELECT 'ALTER TABLE BENUTZER DISABLE CONSTRAINT '||CONSTRAINT_NAME||';' FROM ALL_CONS_COLUMNS WHERE TABLE_NAME = 'BENUTZER' AND COLUMN_NAME = 'SYSBENUTZERTYP_BIT'


SELECT column_name, constraint_name
     FROM user_cons_columns
     WHERE table_name = 'BENUTZER';
     
SELECT count(*) FROM dba_audit_session WHERE USERNAME like 'X_MOBIOIDC%' and OS_USERNAME = 'urr';

SELECT * FROM USER_AUDIT_SESSION WHERE OS_USERNAME = 'urr';