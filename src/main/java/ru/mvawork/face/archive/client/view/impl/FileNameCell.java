package ru.mvawork.face.archive.client.view.impl;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiRenderer;
import ru.mvawork.face.archive.shared.FileInfo;

public class FileNameCell extends AbstractCell<FileInfo> {

    interface ArchiveInfo {
        String getUUID();
    }

    interface Style extends CssResource {
        String blank();
        String folder();
    }

    private final ArchiveInfo archiveInfo;


    interface FNUiRenderer extends UiRenderer {
        Style getStyle();
        void render(SafeHtmlBuilder sb, String name, String iconClass, String target, SafeUri fileSafeUri);
    }

    private static FNUiRenderer renderer = GWT.create(FNUiRenderer.class);


    public FileNameCell(ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
    }

    @Override
    public void render(Context context, FileInfo value, SafeHtmlBuilder sb) {
        String s = URL.encodeQueryString(value.getFullName());
        renderer.render(sb, value.getName(), value.isDirectory() ? renderer.getStyle().folder() : renderer.getStyle().blank(),
                value.isDirectory() ? "_self" : "_blank",
                value.isDirectory() ? UriUtils.fromString("#" + s) : UriUtils.fromString("rest/archive/getFile?uuid=" + URL.encodeQueryString(archiveInfo.getUUID()) + "&fileName=" + s));
    }


}