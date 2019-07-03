ALTER TABLE egbpa_application ADD column permitCancel bigint;

CREATE SEQUENCE SEQ_EGBPA_PERMIT_CANCEL;
CREATE TABLE EGBPA_PERMIT_CANCEL
(
  id bigint NOT NULL,
  application bigint NOT NULL,
  applicationdate date NOT NULL,
  cancellationdate date,
  initiatorRemarks character varying(1024) NOT NULL,
  approverRemarks character varying(1024),
  acceptance boolean default false,
  createdby bigint NOT NULL,
  createddate timestamp without time zone NOT NULL,
  lastmodifiedby bigint,
  lastmodifieddate timestamp without time zone,
  version numeric DEFAULT 0,
  CONSTRAINT pk_egbpa_permit_cancellation PRIMARY KEY (id),
  CONSTRAINT fk_egbpa_permit_cancellation_appln FOREIGN KEY (application)
      REFERENCES egbpa_application (id),
   CONSTRAINT fk_egbpa_permit_cancellation_crtby FOREIGN KEY (createdby)
      REFERENCES eg_user (id),
  CONSTRAINT fk_egbpa_permit_cancellation_mdfdby FOREIGN KEY (lastmodifiedby)
      REFERENCES eg_user (id)
);

ALTER TABLE egbpa_application ADD CONSTRAINT fk_appln_permitCancel FOREIGN KEY (permitCancel)
   REFERENCES EGBPA_PERMIT_CANCEL (id);

CREATE TABLE egbpa_permit_cancel_document
(
  permitCancel bigint,
  fileStore bigint,
CONSTRAINT fk_permit_cancel_id FOREIGN KEY (permitCancel)
      REFERENCES EGBPA_PERMIT_CANCEL (id),
CONSTRAINT fk_permit_cancel_filemapper FOREIGN KEY (fileStore)
      REFERENCES eg_filestoremap (id)
);