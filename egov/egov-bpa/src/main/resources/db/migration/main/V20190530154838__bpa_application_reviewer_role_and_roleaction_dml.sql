INSERT INTO eg_role(id, name, description, createddate, createdby, lastmodifiedby, lastmodifieddate, version) VALUES (nextval('seq_eg_role'), 'BPA Reviewer', 'BPA Reviewer', now(), 1, 1, now(), 0);


Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Search for Occupancy Certificate Application'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='get active child boundaries of parent boundary'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='loadLocalityBoundaryByWard'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='View Occupancy Certificate Details'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='OC Comparison Report'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='View Bpa application details by permit number'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='show bpa inspection details'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Download BPA Documents'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='File Download'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Generate oc demand notice'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Generate Occupancy Certificate'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Generate oc rejection notice'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='View BPA Application Details'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='generate demand notice for bpa'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='generate building permit order'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='generate bpa rejection notice'));

Insert into eg_roleaction (roleid,actionid) values ((select id from eg_role where name='BPA Reviewer'), (select id from eg_action where name='Search BPA Application'));