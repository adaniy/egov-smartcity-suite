Insert into eg_action(id,name,url,parentmodule,ordernumber,displayname,enabled,contextroot) 
values(nextval('SEQ_EG_ACTION'),'JournalVoucherSearch','/voucher/voucherSearch-search.action',
null,1,'JournalVoucherSearch',false,'EGF');

Insert into eg_roleaction   values((select id from eg_role where name='Super User'),(select id from eg_action where name='JournalVoucherSearch'));


