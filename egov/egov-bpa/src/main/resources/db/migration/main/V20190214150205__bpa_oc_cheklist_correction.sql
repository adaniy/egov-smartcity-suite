update egbpa_mstr_chklistdetail set description  = 'The height of building - Whether marked correct as specified in KMBR 1999 and related amendments?' where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYRULE') and code = 'OCPLANSCRUTINYRULE-04' ;

update egbpa_mstr_chklistdetail set description  = 'Whether the practicality of the provided car parking, two wheeler parking, loading and unloading space, manoeuvring space, slope, maximum open yard area allowable to be covered by parking, width of driveway etc. are ensured with regards to provisions as per KMBR 1999 and related amendments?' where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYRULE') and code = 'OCPLANSCRUTINYRULE-05' ;


update egbpa_mstr_chklistdetail set description  = 'Whether exit doorways are opened to an enclosed stairway or a horizontal exit or a corridor or passageway providing continuous and protected means of egress as per KMBR 1999 and related amendments?'
where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYRULE') and code = 'OCPLANSCRUTINYRULE-06' ;


update egbpa_mstr_chklistdetail set description  = 'Whether the conditions related to lighting and ventilation as stipulated in KMBR rule 49 and related amendments are complied with?'
where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYRULE') and code = 'OCPLANSCRUTINYRULE-07' ;


update egbpa_mstr_chklistdetail set description  = 'Whether the conditions on incorporating hazardous uses with residential uses as stipulated in rule 53-2 are complied with?'
where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYRULE') and code = 'OCPLANSCRUTINYRULE-08' ;


update egbpa_mstr_chklistdetail set description  = 'Whether all drawings and details, specified as per KMBR are uploaded in the system, as drawings in .pdf format?' where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYDRAWING') and code = 'OCPLANSCRUTINYDRAWING-01' ;

update egbpa_mstr_chklistdetail set description  = 'Whether the various uses/ occupancies of different spaces in the building/s as specified in the generated EDCR report for the particular application, are in accordance with the uses specified against different spaces specified in the uploaded drawings in pdf format, based on occupancy classes defined as per prevailing KMBR rules and related amendments?' where checklist in (select id from egbpa_mstr_checklist where checklisttype ='OCPLANSCRUTINYDRAWING') and code = 'OCPLANSCRUTINYDRAWING-03' ;


update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-01' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-07' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-08' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-04' ;

update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-47' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-41' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-48' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-44' ;

update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-67' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-61' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-68' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-64' ;

update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-127' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-121' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-128' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-124' ;

update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-107' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-101' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-108' ;
update egbpa_mstr_chklistdetail set ismandatory  = true where code = 'OCDOC-104' ;