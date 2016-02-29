package ru.mvawork.service;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import ru.mvawork.service.archive.ArchiveService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("rest")
public class ApplicationConfig extends Application {
    private final Set<Class<?>> classes = new HashSet<>();


    public ApplicationConfig() {
        classes.add(MultiPartFeature.class);
        classes.add(ArchiveService.class);

    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}
