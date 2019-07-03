CREATE SEQUENCE SEQ_EGBPA_NOC_VERIFICATION;
CREATE TABLE EGBPA_NOC_VERIFICATION
(
  id bigint NOT NULL,
  verifiedUserPos bigint,
  verifiedUser bigint,
  remarks character varying(5000),
  version numeric DEFAULT 0,
  createdby bigint NOT NULL,
  createddate timestamp without time zone NOT NULL,
  lastmodifiedby bigint,
  lastmodifieddate timestamp without time zone,
  CONSTRAINT pk_EGBPA_NOC_VERIFICATION_ID PRIMARY KEY (id),
  CONSTRAINT fk_EGBPA_NOC_VERIFICATION_crtby FOREIGN KEY (createdby)
      REFERENCES eg_user (id),
  CONSTRAINT fk_EGBPA_NOC_VERIFICATION_mdfdby FOREIGN KEY (lastmodifiedby)
      REFERENCES eg_user (id)
);

CREATE SEQUENCE SEQ_EGBPA_OC_NOC_VERIFICATION;
CREATE TABLE EGBPA_OC_NOC_VERIFICATION
(
  id bigint NOT NULL,
  oc bigint NOT NULL,
  nocverification bigint NOT NULL,
  version numeric DEFAULT 0,
  createdby bigint NOT NULL,
  createddate timestamp without time zone NOT NULL,
  lastmodifiedby bigint,
  lastmodifieddate timestamp without time zone,
  CONSTRAINT pk_OC_NOC_VERIFICATION_ID PRIMARY KEY (id),
  CONSTRAINT fk_EGBPA_NOC_VERIFICATION_OC FOREIGN KEY (oc)
      REFERENCES egbpa_occupancy_certificate (id),
  CONSTRAINT fk_OC_NOC_VERIFICATION FOREIGN KEY (nocverification)
      REFERENCES EGBPA_NOC_VERIFICATION (id),
  CONSTRAINT fk_OC_NOC_VERIFICATION_crtby FOREIGN KEY (createdby)
      REFERENCES eg_user (id),
  CONSTRAINT fk_OC_NOC_VERIFICATION_mdfdby FOREIGN KEY (lastmodifiedby)
      REFERENCES eg_user (id)
);

CREATE SEQUENCE SEQ_EGBPA_PERMIT_NOC_VERIFICATION;
CREATE TABLE EGBPA_PERMIT_NOC_VERIFICATION
(
  id bigint NOT NULL,
  application bigint NOT NULL,
  nocverification bigint NOT NULL,
  version numeric DEFAULT 0,
  createdby bigint NOT NULL,
  createddate timestamp without time zone NOT NULL,
  lastmodifiedby bigint,
  lastmodifieddate timestamp without time zone,
  CONSTRAINT pk_PERMIT_NOC_VERIFICATION_ID PRIMARY KEY (id),
  CONSTRAINT fk_EGBPA_NOC_VERIFICATION_OC FOREIGN KEY (application)
      REFERENCES egbpa_application (id),
  CONSTRAINT fk_PERMIT_NOC_VERIFICATION FOREIGN KEY (nocverification)
      REFERENCES EGBPA_NOC_VERIFICATION (id),
  CONSTRAINT fk_PERMIT_NOC_VERIFICATION_crtby FOREIGN KEY (createdby)
      REFERENCES eg_user (id),
  CONSTRAINT fk_PERMIT_NOC_VERIFICATION_mdfdby FOREIGN KEY (lastmodifiedby)
      REFERENCES eg_user (id)
);