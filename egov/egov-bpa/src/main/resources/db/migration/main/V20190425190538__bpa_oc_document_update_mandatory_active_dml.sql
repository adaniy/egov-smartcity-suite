update egbpa_mstr_chklistdetail set ismandatory =false where description in ('Possession Certificate','Specification report','Building tax receipt') and checklist in (select id from egbpa_mstr_checklist where checklisttype  ='OCGENERALDOCUMENTS');

update egbpa_mstr_chklistdetail set isactive =false where description in ('Others','Building tax receipt') and checklist in (select id from egbpa_mstr_checklist where checklisttype  ='OCGENERALDOCUMENTS');

update egbpa_mstr_chklistdetail set isactive =false, ismandatory =false where description in ('Building Plan') and checklist in (select id from egbpa_mstr_checklist where checklisttype  ='OCDCRDOCUMENTS');