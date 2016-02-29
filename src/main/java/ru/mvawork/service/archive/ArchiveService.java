package ru.mvawork.service.archive;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.activation.MimetypesFileTypeMap;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@Path("/archive")
@RequestScoped
public class ArchiveService {

    private static final String ARCHIVE_FILE_NAME = "ArchiveFileName";

    @Inject
    private Sessions sessions;

    private MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/load")
    @Produces(MediaType.TEXT_HTML)
    public String load(@FormDataParam(ARCHIVE_FILE_NAME) InputStream fileInputStream,
                       @FormDataParam(ARCHIVE_FILE_NAME) FormDataContentDisposition fileInfo) throws IOException {
        File file = File.createTempFile("ARC", null);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int count;
            while ((count = fileInputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, count);
            }
        }
        return sessions.addFile(new Sessions.LoadedFileInfo(fileInfo.getFileName(), file.getAbsolutePath()));
    }

    @DELETE
    @Path("/unLoad")
    public void unLoad(@QueryParam("uuid") String uuid) {
        sessions.deleteFile(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/getFileList")
    public ArchiveInfo getFileList(@QueryParam("uuid") String uuid) throws IOException, RarException {
        Sessions.LoadedFileInfo loadedFileInfo = sessions.getFile(uuid);
        ArchiveInfo result = new ArchiveInfo(loadedFileInfo.fileName);
        Archive archive = new Archive(new File(loadedFileInfo.localPath));
        List<FileHeader> fileList = archive.getFileHeaders();
        fileList.forEach(result::addFileHeader);
        return result;
    }

    @GET
    @Path("/getFile")
    public Response getFile(@QueryParam("uuid") String uuid, @QueryParam("fileName") String fileName) throws IOException, RarException {
        Sessions.LoadedFileInfo loadedFileInfo = sessions.getFile(uuid);
        Archive archive = new Archive(new File(loadedFileInfo.localPath));
        ByteArrayOutputStream bos = new  ByteArrayOutputStream();
        List<FileHeader> fileList = archive.getFileHeaders();
        FileHeader fileHeader = fileList.stream()
                .filter(s -> (s.getFileNameW().isEmpty() ? s.getFileNameString():s.getFileNameW()).equals(fileName))
                .findAny().get();
        archive.extractFile(fileHeader, bos);
        String s = fileName;
        int i;
        while ((i = s.indexOf('\\')) > 0) {
            s = s.substring(i + 1);
        }

        String cd = "attachment; filename=\"" + s + "\"; filename*=UTF-8''" + URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        String mt = mimetypesFileTypeMap.getContentType(s);
        return Response.ok(bos.toByteArray()).header("Content-Type", mt).header("Content-Disposition", cd).build();
    }

}
