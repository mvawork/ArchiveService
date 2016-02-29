PL/SQL Developer Test script 3.0
15
declare 
 A TArchiver;
 X xmltype;
 uuid Varchar2(255);
begin
 for i in (Select * 
           from TEST_ARCHIVE t)
 loop           
  A := TArchiver(:BaseUrl, 60);
  uuid := A.LoadArchive(i.filename, i.filebody);
  :X := A.getFileList(uuid).getclobval();
  :FB := A.getFile(uuid, :FN);
  A.UnLoadArchive(uuid);
 end loop;
end;
4
BaseUrl
1
http://172.16.12.59:8080/UnRARService
5
FN
1
Тест RAR\Каталог 1\Файл 1.txt
5
X
1
<CLOB>
112
FB
1
<BLOB>
113
0
