-----Permit Application workflow matrix--------------
update eg_wf_matrix set validactions ='Approve,Revert,Reject,Forward to Overseer,Forward to Superintendent' where nextstate='Final Approval Process initiated' and nextaction='Permit Fee Collection Pending' and objecttype ='BpaApplication' and additionalrule='CREATEBPAAPPLICATION';

INSERT INTO eg_wf_matrix (id, department, objecttype, currentstate, currentstatus, pendingactions, currentdesignation, additionalrule, nextstate, nextaction, nextdesignation, nextstatus, validactions, fromqty, toqty, fromdate, todate)
 VALUES (nextval('seq_eg_wf_matrix'), 'ANY', 'BpaApplication', 'Initiated for noc verification', 'Noc Updated', 'Forwarded to overseer for noc verification', 'Town Planning Building Overseer', 'CREATEBPAAPPLICATION', 'Initiated for noc verification', 'Forwarded to overseer for noc verification', 'Town Planning Building Overseer', 'Noc Verification Initiated', 'Forward to Initiator', NULL, NULL, now(), '2099-04-01');

INSERT INTO eg_wf_matrix (id, department, objecttype, currentstate, currentstatus, pendingactions, currentdesignation, additionalrule, nextstate, nextaction, nextdesignation, nextstatus, validactions, fromqty, toqty, fromdate, todate)
 VALUES (nextval('seq_eg_wf_matrix'), 'ANY', 'BpaApplication', 'Initiated for noc verification', 'Noc Updated', 'Forwarded to superintendent for noc updation', 'Superintendent', 'CREATEBPAAPPLICATION', 'Initiated for noc verification', 'Forwarded to superintendent for noc updation', 'Superintendent', 'Noc Verification Initiated', 'Forward to Initiator', NULL, NULL, now(), '2099-04-01');


-----Occupancy certificate Application workflow matrix--------------
update eg_wf_matrix set validactions ='Approve,Forward to Overseer,Forward to Superintendent,Revert,Reject' where nextstatus='Approved' and nextstate='Record Approved' and objecttype ='OccupancyCertificate';

INSERT INTO eg_wf_matrix (id, department, objecttype, currentstate, currentstatus, pendingactions, currentdesignation, additionalrule, nextstate, nextaction, nextdesignation, nextstatus, validactions, fromqty, toqty, fromdate, todate)
 VALUES (nextval('seq_eg_wf_matrix'), 'ANY', 'OccupancyCertificate', 'Initiated for noc verification', 'Noc Updated', 'Forwarded to overseer for noc verification', 'Town Planning Building Overseer', 'OCCUPANCYCERTIFICATE', 'Initiated for noc verification', 'Forwarded to overseer for noc verification', 'Town Planning Building Overseer', 'Noc Verification Initiated', 'Forward to Initiator', NULL, NULL, now(), '2099-04-01');

INSERT INTO eg_wf_matrix (id, department, objecttype, currentstate, currentstatus, pendingactions, currentdesignation, additionalrule, nextstate, nextaction, nextdesignation, nextstatus, validactions, fromqty, toqty, fromdate, todate)
 VALUES (nextval('seq_eg_wf_matrix'), 'ANY', 'OccupancyCertificate', 'Initiated for noc verification', 'Noc Updated', 'Forwarded to superintendent for noc updation', 'Superintendent', 'OCCUPANCYCERTIFICATE', 'Initiated for noc verification', 'Forwarded to superintendent for noc updation', 'Superintendent', 'Noc Verification Initiated', 'Forward to Initiator', NULL, NULL, now(), '2099-04-01');