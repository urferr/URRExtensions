select * from AA a where a.SYSAATYPE_ID = '1051000613400002';

select * from TABBEZ t where t.ref_id = '129F0000000A0004';
select * from AASTRUCTURE ast where ast.top_aa_id = '129F0000000A0001';
select * from AASTRUCTURE ast where ast.child_ref_id = '129F0000000A0004';
select * from AANODE an where an.aanode_id in (select ast.PARENT_AANODE_ID from AASTRUCTURE ast where ast.top_aa_id = '129F0000000A0001');
select * from AANODE an where an.aa_id = '129F0000000A0004';
select * from AANODEHIST ah where ah.aanode_id in (select an.AANODE_ID from AANODE an where an.aanode_id in (select ast.PARENT_AANODE_ID from AASTRUCTURE ast where ast.top_aa_id = '129F0000000A0001'));
select * from AA a2 where a2.AA_ID in (select ast.CHILD_REF_ID from AASTRUCTURE ast where ast.top_aa_id = '129F0000000A0001');
