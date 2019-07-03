alter table egbpa_mstr_servicetype add column amenitySelectionRequired boolean default false;
alter table egbpa_mstr_servicetype add column occupancySelectionRequired boolean default false;
alter table egbpa_mstr_servicetype add column isEdcrMandatory boolean default false;

update egbpa_mstr_servicetype set amenitySelectionRequired=true,occupancySelectionRequired=true where code in ('01','03','04','06','07','09');
update egbpa_mstr_servicetype set amenitySelectionRequired=false,occupancySelectionRequired=false where code in ('14','15');
update egbpa_mstr_servicetype set amenitySelectionRequired=true,occupancySelectionRequired=false where code in ('08');
update egbpa_mstr_servicetype set amenitySelectionRequired=false,occupancySelectionRequired=true where code in ('02','05');

alter table egbpa_mstr_servicetype add column planScrutinyDocumentsRequired boolean default false;
update egbpa_mstr_servicetype set planScrutinyDocumentsRequired=true where code in ('01','03','04','06','07','05','08','14','15');