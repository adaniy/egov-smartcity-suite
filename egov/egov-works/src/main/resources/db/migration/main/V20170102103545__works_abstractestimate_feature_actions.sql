INSERT INTO eg_feature_action (ACTION, FEATURE) VALUES ((select id FROM eg_action  WHERE name = 'ShowHideApproverDetails') ,(select id FROM eg_feature WHERE name = 'Create Abstract/Detailed Estimate'));
INSERT INTO eg_feature_action (ACTION, FEATURE) VALUES ((select id FROM eg_action  WHERE name = 'ShowHideApproverDetails') ,(select id FROM eg_feature WHERE name = 'Update Abstract/Detailed Estimate'));

--rollback delete FROM eg_feature_action WHERE ACTION = (select id FROM eg_action  WHERE name = 'ShowHideApproverDetails') and FEATURE = (select id FROM eg_feature WHERE name = 'Create Abstract/Detailed Estimate');
--rollback delete FROM eg_feature_action WHERE ACTION = (select id FROM eg_action  WHERE name = 'ShowHideApproverDetails') and FEATURE = (select id FROM eg_feature WHERE name = 'Update Abstract/Detailed Estimate');