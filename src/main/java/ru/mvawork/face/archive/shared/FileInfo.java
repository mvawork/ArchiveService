package ru.mvawork.face.archive.shared;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class FileInfo {

    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "isDirectory")
    private boolean isDirectory;
    @XmlElement(name = "FileInfo")
    private List<FileInfo> subList;

    @XmlTransient
    private FileInfo root;

    public FileInfo(FileInfo root, String name, boolean isDirectory) {
        this.root = root;
        this.name = name;
        this.isDirectory = isDirectory;
    }


    @SuppressWarnings("unused")
    public FileInfo() {
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(name);
        FileInfo r = this;
        while ((r = r.getRoot()) != null && r.getRoot() != null)
            sb.insert(0, r.getName() + "\\");
        return sb.toString();
    }

    public static FileInfo find(FileInfo fileInfo, String name) {

        String s = fileInfo.getFullName();
        if (name.equals(s))
            return fileInfo;

        FileInfo result = null;
        for (FileInfo f : fileInfo.getSubList()) {
            if (name.equals(s + "\\" + name)) {
                result = f;
                break;
            }
            if (f.isDirectory() && (result = find(f, name)) != null)
                break;
        }
        return result;
    }

    public List<FileInfo> getSubList() {
        if (subList == null) {
            subList = new ArrayList<>();
        }
        return subList;
    }

    public void addFile(String fileName) {
        getSubList().add(new FileInfo(this, fileName, false));
    }

    public FileInfo addSubDir(String subDir) {
        for (FileInfo i : getSubList())
            if (i.getName().equals(subDir))
                return i;
        FileInfo result = new FileInfo(this, subDir, true);
        getSubList().add(result);
        return result;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public FileInfo getRoot() {
        return root;
    }


}
