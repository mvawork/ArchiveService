create or replace type TArchiver as object (

 BaseUrl Varchar2(4000),
 timeout Number,
 
 constructor function TArchiver(BaseUrl Varchar2, TimeOut Number) return self as result,
 member function doGet(RequestUrl VarChar2, ResponseContent OUT BLOB, ResponseContentType OUT VarChar2) return Number,
 member function doPost(RequestUrl VarChar2, RequestContent BLOB, RequestContentType Varchar2,
                                              ResponseContent OUT BLOB, ResponseContentType OUT VarChar2)  return Number,
 member function doDelete(RequestUrl VarChar2) return Number,                                               
 member function LoadArchive(ArcFileName Varchar2, ArcFileBody Blob) return Varchar2,
 member function getFileList(uuid Varchar2) return XMLType, 
 member function getFile(uuid Varchar2, FileName Varchar2) return Blob, 
 member procedure UnLoadArchive(uuid Varchar2)
 
)
/
create or replace type body TArchiver is


 member function doGet(RequestUrl VarChar2, ResponseContent OUT BLOB, ResponseContentType OUT VarChar2) return Number
  is
  req       UTL_HTTP.REQ;
  resp      UTL_HTTP.RESP;
  temp_blob Blob;
  res       Number(3);
 begin 
  UTL_HTTP.set_transfer_timeout(timeout);
  req := UTL_HTTP.BEGIN_REQUEST(RequestUrl, 'GET');
  UTL_HTTP.SET_PERSISTENT_CONN_SUPPORT(req, TRUE);
  resp := UTL_HTTP.GET_RESPONSE(req);
  ResponseContent := null;
  LOOP
   begin
    DBMS_LOB.CREATETEMPORARY(temp_blob, TRUE, dbms_lob.CALL);
    UTL_HTTP.read_raw(resp, temp_BLOB);
    if temp_BLOB is not null then
     if ResponseContent is Null then
      DBMS_LOB.CREATETEMPORARY(ResponseContent, TRUE, dbms_lob.CALL);
     end if;
     dbms_lob.append(ResponseContent, temp_BLOB);
    end if;
   EXCEPTION
    WHEN UTL_HTTP.END_OF_BODY THEN
     exit;
    WHEN UTL_HTTP.TRANSFER_TIMEOUT then
     exit;
   end;
  END LOOP;
  UTL_HTTP.get_header_by_name(resp, 'Content-Type', ResponseContentType);
  res := resp.status_code;
  UTL_HTTP.END_RESPONSE(resp);
  return res;
 end; 
 
 member function doDelete(RequestUrl VarChar2) return Number
  is
  req       UTL_HTTP.REQ;
  resp      UTL_HTTP.RESP;
  temp_blob Blob;
  res       Number(3);
 begin 
  UTL_HTTP.set_transfer_timeout(timeout);
  req := UTL_HTTP.BEGIN_REQUEST(RequestUrl, 'DELETE');
  UTL_HTTP.SET_PERSISTENT_CONN_SUPPORT(req, TRUE);
  resp := UTL_HTTP.GET_RESPONSE(req);
  res := resp.status_code;
  UTL_HTTP.END_RESPONSE(resp);
  return res;
 end; 
 
 member function doPost(RequestUrl VarChar2, RequestContent BLOB, RequestContentType Varchar2, ResponseContent OUT BLOB, ResponseContentType OUT VarChar2)  return Number
 is
  req       UTL_HTTP.REQ;
  resp      UTL_HTTP.RESP;
  temp_BLOB BLOB;
  i         PLS_INTEGER;
  len       PLS_INTEGER;
  MAX_RAW   CONSTANT PLS_INTEGER := 32000;
  res       Number(3);   
 begin
  if RequestContent is not null then
   Len := dbms_lob.getLength(RequestContent);
  else
   Len := 0;
  end if;
  i := 1;
  UTL_HTTP.set_transfer_timeout(timeout);
  req := UTL_HTTP.BEGIN_REQUEST(RequestUrl, 'POST');
  UTL_HTTP.SET_PERSISTENT_CONN_SUPPORT(req, TRUE);
  UTL_HTTP.SET_HEADER(req, 'Content-Type', RequestContentType);
  UTL_HTTP.SET_HEADER(req, 'Content-Length', Len);
  WHILE (i < len) LOOP
   UTL_HTTP.write_raw(req, dbms_lob.substr(RequestContent, MAX_RAW, i));
   i := i + MAX_RAW;
  END LOOP;
  resp := UTL_HTTP.GET_RESPONSE(req);
  ResponseContent := null;
  LOOP
   begin
    DBMS_LOB.CREATETEMPORARY(temp_blob, TRUE, dbms_lob.CALL);
    UTL_HTTP.read_raw(resp, temp_BLOB);
    if temp_BLOB is not null then
     if ResponseContent is Null then
      DBMS_LOB.CREATETEMPORARY(ResponseContent, TRUE, dbms_lob.CALL);
     end if;
     dbms_lob.append(ResponseContent, temp_BLOB);
    end if;
   EXCEPTION
    WHEN UTL_HTTP.END_OF_BODY THEN
     exit;
    WHEN UTL_HTTP.TRANSFER_TIMEOUT then
     exit;
   end;
  END LOOP;
  UTL_HTTP.get_header_by_name(resp, 'Content-Type', ResponseContentType);
  res := resp.status_code;
  UTL_HTTP.END_RESPONSE(resp);
  return res;
 end;
  
 constructor function TArchiver(BaseUrl Varchar2, TimeOut Number) return self as result
  is
 begin 
  Self.BaseUrl := BaseUrl;
  Self.TimeOut := TimeOut; 
  return; 
 end;
 
 member function LoadArchive(ArcFileName Varchar2, ArcFileBody Blob) return Varchar2
  is 
  boundary Varchar2(255) := '0123456789abcdefgh';
  RequestContent  Blob;
  ResponseContent Blob;
  ResponseContentType Varchar2(255);
  R raw(32000);
  rn             Varchar2(2) := Chr(13) || Chr(10);
  Res Number(3);
 begin 
  dbms_lob.createtemporary(RequestContent, true, dbms_lob.CALL);
  R := UTL_RAW.cast_to_raw(Convert(rn || 
                          '--' || boundary || rn || 
                          'Content-Disposition: form-data; name="ArchiveFileName"; filename="' || ArcFileName || '"' || rn || 
                          'Content-Type: application/octet-stream' || rn ||
                          'Content-Transfer-Encoding: binary' || rn || rn, 'UTF8'));
   
  Dbms_Lob.WriteAppend(RequestContent, UTL_RAW.length(R), R);
  
  Dbms_Lob.Append(RequestContent, ArcFileBody);
  
  R := UTL_RAW.cast_to_raw(Convert(rn || '--' || boundary || '--' || rn, 'UTF8'));
  Dbms_Lob.writeappend(RequestContent, UTL_RAW.length(R), R);
  
  Res := doPost(utl_url.escape(BaseUrl || '/rest/archive/load', false, 'UTF8'), RequestContent, 'multipart/form-data; boundary=' || boundary, 
   ResponseContent, ResponseContentType);
  if Res <> UTL_HTTP.HTTP_OK or ResponseContentType <> 'text/html' then 
   Raise_Application_Error(-20101, 'Ошибка загрузки архива');
  end if;
  Return utl_raw.cast_to_varchar2(dbms_lob.substr(ResponseContent));
 end;
 
 member function getFileList(uuid Varchar2) return XMLType
  is
  ResponseContent Blob;
  ResponseContentType Varchar2(255);
  Res Number(3);
 begin
  Res := doGet(utl_url.escape(BaseUrl || '/rest/archive/getFileList?uuid=', false, 'UTF8') || utl_url.escape(uuid, true, 'UTF8'), ResponseContent, ResponseContentType);
  if Res <> UTL_HTTP.HTTP_OK or ResponseContentType <> 'application/xml' then 
   Raise_Application_Error(-20101, 'Ошибка получения списка файлов');
  end if;    
  Return XMLTYPE(ResponseContent, nls_charset_id('UTF8'));
 end; 
 
 member function getFile(uuid Varchar2, FileName Varchar2) return Blob  
  is
  Res  Number(3);
  ResponseContent Blob;
  ResponseContentType Varchar2(255);
 begin 
  Res := doGet(utl_url.escape(BaseUrl || '/rest/archive/getFile?uuid=', false, 'UTF8') || utl_url.escape(uuid, true, 'UTF8') || '&fileName=' || utl_url.escape(FileName, true, 'UTF8'),
   ResponseContent, ResponseContentType);
  if Res <> UTL_HTTP.HTTP_OK  then 
   Raise_Application_Error(-20101, 'Ошибка получения файла');
  end if;    
  Return ResponseContent;
 end; 
 
 member procedure UnLoadArchive(uuid Varchar2) 
  is
  Res Number(3);
 begin
  Res := doDelete(utl_url.escape(BaseUrl || '/rest/archive/unLoad?uuid=', false, 'UTF8') || utl_url.escape(uuid, true, 'UTF8'));
  if Res <> UTL_HTTP.HTTP_NO_CONTENT  then 
   Raise_Application_Error(-20101, 'Ошибка удаления архива');
  end if;    
 end; 
 
end;
/
