Insert into eg_action (id,name,url,queryparams,parentmodule,ordernumber,displayname,enabled,contextroot,version,createdby,createddate,lastmodifiedby,lastmodifieddate,application) values (nextval('SEQ_EG_ACTION'),'ChequeAssignmentSave','/payment/chequeAssignment-save.action',null,(select id from eg_module where name='Payments'),1,'ChequeAssignmentSave',false,'EGF',0,1,current_date,1,current_date,(select id from eg_module where name = 'EGF' and parentmodule is null));
Insert into eg_roleaction values((select id from eg_role where name='Super User'),(select id from eg_action where name='ChequeAssignmentSave'));


