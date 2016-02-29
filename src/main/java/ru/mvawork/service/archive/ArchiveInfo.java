package ru.mvawork.service.archive;

import com.github.junrar.rarfile.FileHeader;
import ru.mvawork.face.archive.shared.FileInfo;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "ArchveInfo")
@XmlAccessorType(XmlAccessType.FIELD)
class ArchiveInfo {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @XmlTransient
    private String name;

    @XmlElement(name = "FileInfo")
    private FileInfo rootDir;

    private FileInfo getRootDir() {
        if (rootDir == null)
            rootDir = new FileInfo(null, name, true);
        return rootDir;
    }


    ArchiveInfo(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public ArchiveInfo() {

    }


    void addFileHeader(FileHeader fileHeader) {
        String fileName = fileHeader.getFileNameW().isEmpty() ? fileHeader.getFileNameString() : fileHeader.getFileNameW() ;
        FileInfo fileDir = getRootDir();
        int i;
        while ((i = fileName.indexOf('\\')) > 0) {
            String subDir = fileName.substring(0, i);
            fileDir = fileDir.addSubDir(subDir);
            fileName = fileName.substring(i + 1);
        }
        if (fileHeader.isDirectory()) {
            fileDir.addSubDir(fileName);
        }  else {
            fileDir.addFile(fileName);
        }
    }
}
