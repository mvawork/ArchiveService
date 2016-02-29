package ru.mvawork.service.archive;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Sessions {

    static class LoadedFileInfo {
        String fileName;
        String localPath;

        public LoadedFileInfo(String fileName, String localPath) {
            this.fileName = fileName;
            this.localPath = localPath;
        }
    }


    ConcurrentHashMap<String, LoadedFileInfo> loadedFiles = new ConcurrentHashMap<>();

    @PostConstruct
    private void postConstract() {

    }

    @PreDestroy
    private void preDestroy() {
        Collection<LoadedFileInfo> files = loadedFiles.values();
        files.forEach(s -> new File(s.fileName).delete());
    }

    String addFile(LoadedFileInfo file) {
        String result = UUID.randomUUID().toString();
        loadedFiles.put(result, file);
        return result;
    }

    LoadedFileInfo getFile(String uuid) {
        return loadedFiles.get(uuid);
    }

    public void deleteFile(String uuid) {
        File file = new File(getFile(uuid).localPath);
        loadedFiles.remove(uuid);
        file.delete();
    }
}
