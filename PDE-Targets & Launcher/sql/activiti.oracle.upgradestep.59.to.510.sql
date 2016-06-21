alter table ACT_RU_IDENTITYLINK
add PROC_DEF_ID_ NVARCHAR2(64);

create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_);
create index ACT_IDX_ATHRZ_PROCEDEF  on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);
    
alter table ACT_RU_EXECUTION 
	add CACHED_ENT_STATE_ INTEGER;

update ACT_RU_EXECUTION set CACHED_ENT_STATE_ = 7;
	
alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_);
    
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);

update ACT_GE_PROPERTY set value_='5.10', rev_=2 where name_='schema.version';
update ACT_GE_PROPERTY set value_='create(5.9) upgrade(5.9->5.10)', rev_=2 where name_='schema.history';
update ACT_GE_PROPERTY set value_='0', rev_=2 where name_='historyLevel';
