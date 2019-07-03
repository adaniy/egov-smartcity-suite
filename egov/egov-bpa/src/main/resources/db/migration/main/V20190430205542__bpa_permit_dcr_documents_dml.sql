insert into EGBPA_MSTR_CHECKLIST(id,checklisttype,servicetype,version,createdBy,createdDate)
values(nextval('SEQ_EGBPA_MSTR_CHECKLIST'),'BPADCRDOCUMENTS', (select id from egbpa_mstr_servicetype where code='05'),
0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-81','Site Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-82','Service Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-83','Key Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-84','Subdivision Plan',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-85','Section Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-86','Contour/Topographic Plan',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='05')), 0,1,now());


insert into EGBPA_MSTR_CHECKLIST(id,checklisttype,servicetype,version,createdBy,createdDate)
values(nextval('SEQ_EGBPA_MSTR_CHECKLIST'),'BPADCRDOCUMENTS', (select id from egbpa_mstr_servicetype where code='08'),
0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-91','Site Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-92','Service Plan',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-93','Plan Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-94','Elevation Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-95','Section Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-96','Others',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='08')), 0,1,now());


insert into EGBPA_MSTR_CHECKLIST(id,checklisttype,servicetype,version,createdBy,createdDate)
values(nextval('SEQ_EGBPA_MSTR_CHECKLIST'),'BPADCRDOCUMENTS', (select id from egbpa_mstr_servicetype where code='14'),
0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-101','Site Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-102','Service Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-103','Plan Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-104','Elevation Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-105','Section Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-106','Others',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='14')), 0,1,now());


insert into EGBPA_MSTR_CHECKLIST(id,checklisttype,servicetype,version,createdBy,createdDate)
values(nextval('SEQ_EGBPA_MSTR_CHECKLIST'),'BPADCRDOCUMENTS', (select id from egbpa_mstr_servicetype where code='15'),
0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-111','Site Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-112','Service Plan',
true,true,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-113','Plan Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-114','Elevation Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-115','Section Details',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());

insert into EGBPA_MSTR_CHKLISTDETAIL (id ,code,description,isactive,ismandatory,checklist,version,createdBy,createdDate)
values((nextval('SEQ_EGBPA_MSTR_CHKLISTDETAIL')),'DCR-116','Others',
true,false,(select id from EGBPA_MSTR_CHECKLIST where checklisttype='BPADCRDOCUMENTS' 
and servicetype= (select id from egbpa_mstr_servicetype where code='15')), 0,1,now());