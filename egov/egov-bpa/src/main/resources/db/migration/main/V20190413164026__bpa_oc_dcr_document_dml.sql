update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-02';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-08';
update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-42';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-48';
update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-62';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-68';
update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-122';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-128';
update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-152';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-158';
update egbpa_mstr_chklistdetail set ismandatory=true where code='OCDOC-172';
update egbpa_mstr_chklistdetail set isactive =false, ismandatory=false where code='OCDOC-178';

update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-01';
update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-02';
update egbpa_mstr_chklistdetail set description='Roof Plan' where code='DCR-OC-05';
update egbpa_mstr_chklistdetail set ismandatory=true,description='Floor Plans, Elevations, Sections' where code='DCR-OC-06';
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-07', 'Details Plan', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='01')), 0, 1, now(), 1, now());
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-8', 'Other Details', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='01')), 0, 1, now(), 1, now());

update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-21';
update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-22';
update egbpa_mstr_chklistdetail set description='Roof Plan' where code='DCR-OC-25';
update egbpa_mstr_chklistdetail set ismandatory=true,description='Floor Plans, Elevations, Sections' where code='DCR-OC-26';
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-27', 'Details Plan', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='03')), 0, 1, now(), 1, now());
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-28', 'Other Details', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='03')), 0, 1, now(), 1, now());

update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-41';
update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-42';
update egbpa_mstr_chklistdetail set description='Roof Plan' where code='DCR-OC-45';
update egbpa_mstr_chklistdetail set ismandatory=true,description='Floor Plans, Elevations, Sections' where code='DCR-OC-46';
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-47', 'Details Plan', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='04')), 0, 1, now(), 1, now());
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-48', 'Other Details', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='04')), 0, 1, now(), 1, now());

update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-61';
update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-62';
update egbpa_mstr_chklistdetail set description='Roof Plan' where code='DCR-OC-65';
update egbpa_mstr_chklistdetail set ismandatory=true,description='Floor Plans, Elevations, Sections' where code='DCR-OC-66';
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-67', 'Details Plan', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='06')), 0, 1, now(), 1, now());
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-68', 'Other Details', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='06')), 0, 1, now(), 1, now());

update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-81';
update egbpa_mstr_chklistdetail set ismandatory=true where code='DCR-OC-82';
update egbpa_mstr_chklistdetail set description='Roof Plan' where code='DCR-OC-85';
update egbpa_mstr_chklistdetail set ismandatory=true,description='Floor Plans, Elevations, Sections' where code='DCR-OC-86';
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-87', 'Details Plan', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='07')), 0, 1, now(), 1, now());
INSERT INTO egbpa_mstr_chklistdetail(id, code, description, isactive, ismandatory, checklist, version, createdby, createddate, lastmodifiedby, lastmodifieddate) VALUES (nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL'), 'DCR-OC-88', 'Other Details', true, false, (select id from egbpa_mstr_checklist where checklisttype='OCDCRDOCUMENTS' and servicetype= (select id from egbpa_mstr_servicetype where code='07')), 0, 1, now(), 1, now());