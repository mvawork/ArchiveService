package ru.mvawork.face.archive.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import ru.mvawork.face.archive.shared.FileInfo;

public interface FileView extends IsWidget {

    void setFileInfo(String uuid, FileInfo fileInfo);
    void switchDirectory(String directory);

}
