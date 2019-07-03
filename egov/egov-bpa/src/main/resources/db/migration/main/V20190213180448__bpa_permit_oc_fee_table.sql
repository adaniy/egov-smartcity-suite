create table EGBPA_OCCUPANCY_FEE
	(
	  id bigint NOT NULL,
	  oc bigint,
	  applicationfeecommon bigint,
	  createdby bigint NOT NULL,
      createddate timestamp without time zone NOT NULL,
 	  lastModifiedDate timestamp without time zone,
 	  lastModifiedBy bigint,
	  version numeric NOT NULL,
	  CONSTRAINT PK_OCCUPANCY_FEE_ID PRIMARY KEY (ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_OC FOREIGN KEY (oc) REFERENCES EGBPA_OCCUPANCY_CERTIFICATE(ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_APPLFEE_COMMON FOREIGN KEY (applicationfeecommon) REFERENCES EGBPA_APPLICATION_FEE_COMMON(ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_MDFDBY FOREIGN KEY (lastModifiedBy) REFERENCES EG_USER (ID),
      CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_CRTBY FOREIGN KEY (createdBy)REFERENCES EG_USER (ID)
   );
 create sequence SEQ_EGBPA_OCCUPANCY_FEE;
