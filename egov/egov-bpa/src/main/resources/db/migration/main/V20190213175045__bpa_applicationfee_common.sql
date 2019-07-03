CREATE TABLE EGBPA_APPLICATION_FEE_COMMON
   (	
     id bigint NOT NULL, 
	 feedate  date DEFAULT ('now'::text)::date NOT NULL,
	 feeremarks character varying(1024),
	 challannumber character varying(128),
	 status bigint NOT NULL,
     isrevised boolean DEFAULT false,
     state_id bigint,
     modifyFeeReason character varying(512),
	 version numeric DEFAULT 0,
	 createdBy bigint NOT NULL,
	 createdDate timestamp without time zone NOT NULL,
	 lastModifiedBy bigint,
     lastModifiedDate timestamp without time zone,
	 CONSTRAINT pk_EGBPA_APPLICATION_FEE_COMMON PRIMARY KEY (id),
	 CONSTRAINT fk_applicationfee_status FOREIGN KEY (status) REFERENCES EGBPA_STATUS (id),
	 CONSTRAINT FK_EGBPA_APPFEE_COMMON_MDFDBY FOREIGN KEY (lastModifiedBy)
         REFERENCES EG_USER (ID),
    CONSTRAINT FK_EGBPA_APPFEE_COMMON_CRTBY FOREIGN KEY (createdBy)
         REFERENCES EG_USER (ID)
   );
CREATE INDEX IDX_EGBPA_APPLICATION_FEE__COMMON_ID  ON EGBPA_APPLICATION_FEE_COMMON USING btree (id);  
CREATE SEQUENCE SEQ_EGBPA_APPLICATION_FEE_COMMON;




CREATE TABLE EGBPA_APPLICATION_FEEDETAILS_COMMON
   (	
     id bigint NOT NULL, 
     bpaFee bigint NOT NULL,
     applicationFeeCommon bigint,
     amount double precision,
	 version numeric DEFAULT 0,
	 createdBy bigint NOT NULL,
	 createdDate timestamp without time zone NOT NULL,
	 lastModifiedBy bigint,
     lastModifiedDate timestamp without time zone,
	 CONSTRAINT pk_EGBPA_APPLICATION_FEEDETAILS_COMMON PRIMARY KEY (id),
	 CONSTRAINT fk_application_feedetails_appfee_common FOREIGN KEY (applicationFeeCommon) REFERENCES EGBPA_APPLICATION_FEE_COMMON (id),
	 CONSTRAINT fk_application_feedetails_bpafee_common FOREIGN KEY (bpaFee) REFERENCES EGBPA_MSTR_BPAFEE (id),
	 CONSTRAINT FK_EGBPA_APPFEEDTL_MDFDBY FOREIGN KEY (lastModifiedBy)
         REFERENCES EG_USER (ID),
    CONSTRAINT FK_EGBPA_APPFEEDTL_CRTBY FOREIGN KEY (createdBy)
         REFERENCES EG_USER (ID)
   );
CREATE INDEX IDX_EGBPA_APPLICATION_FEEDETAILS_COMMON_ID  ON EGBPA_APPLICATION_FEEDETAILS_COMMON USING btree (id);  
CREATE SEQUENCE SEQ_EGBPA_APPLICATION_FEEDETAILS_COMMON;


CREATE TABLE egbpa_application_feedetails_common_aud
(
  id bigint NOT NULL,
  rev integer NOT NULL,
  bpafee bigint,
  applicationfeeCommon bigint,
  amount double precision,
  revtype numeric,
  lastmodifieddate timestamp without time zone,
  lastmodifiedby bigint,
  CONSTRAINT pk_egbpa_application_feedetails_common_aud PRIMARY KEY (id, rev)
);

