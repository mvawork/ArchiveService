package ru.mvawork.face.archive.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class MainEntryPoint implements EntryPoint {
    public void onModuleLoad() {
        RootPanel.get().add(new Application());
    }
}
