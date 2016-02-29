package ru.mvawork.face.archive.client.view.impl;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import ru.mvawork.face.archive.client.view.FileView;
import ru.mvawork.face.archive.shared.FileInfo;

import java.util.List;
import java.util.Vector;

public class FileViewImpl extends Composite implements FileView, FileNameCell.ArchiveInfo {


    @SuppressWarnings("GwtCssResourceErrors")
    interface Style extends CssResource {
        String locationDelimiter();
    }

    @SuppressWarnings("WeakerAccess")
    interface FileViewImplUiBinder extends UiBinder<HTMLPanel, FileViewImpl> { }

    private static FileViewImplUiBinder ourUiBinder = GWT.create(FileViewImplUiBinder.class);

    @UiField
    FlowPanel detailPanel;
    @UiField
    FlowPanel headerPanel;
    @UiField(provided = true)
    CellTable<FileInfo> cellTable;
    @UiField
    FlowPanel browseLocation;

    @UiField
    Style style;
    @UiField
    Label currentLocation;


    private String uuid;
    private FileInfo fileInfo;

    private Vector<Widget> locationWidget = new Vector<>();

    @Override
    public void setFileInfo(String uuid, FileInfo fileInfo) {
        this.uuid = uuid;
        this.fileInfo = fileInfo;
    }

    @Override
    public void switchDirectory(String directory) {

        if (directory == null || directory.isEmpty())
            return;

        FileInfo f = FileInfo.find(fileInfo, directory);
        if (f == null || !f.isDirectory())
            return;

        for(Widget w : locationWidget)
            browseLocation.remove(w);

        locationWidget.clear();


        currentLocation.setText(f.getName());
        Widget w = currentLocation;
        FileInfo r = f;
        while ((r = r.getRoot()) != null) {
            Label i = new Label();
            i.setStyleName(style.locationDelimiter());
            browseLocation.insert(i, browseLocation.getWidgetIndex(w));
            locationWidget.add(i);
            w = i;
            Hyperlink h = new Hyperlink(r.getName(), r.getFullName());
            browseLocation.insert(h, browseLocation.getWidgetIndex(w));
            w = h;
            locationWidget.add(h);
        }
        List<FileInfo> l = listDataProvider.getList();
        l.clear();
        l.addAll(f.getSubList());
    }

    @Override
    public String getUUID() {
        return uuid;
    }



    private ListDataProvider<FileInfo> listDataProvider;

    private Column<FileInfo, FileInfo> buildColumnName() {
        return new Column<FileInfo, FileInfo>(new FileNameCell(this)) {
            @Override
            public FileInfo getValue(FileInfo object) {
                return object;
            }
        };
    }

    private Header<String> buildHeader(final String title) {
        return new Header<String>(new TextCell()) {
            @Override
            public String getValue() {
                return title;
            }
        };
    }

    private void buildTable() {
        buildColumnName();
        cellTable.addColumn(buildColumnName(), buildHeader("Имя"));
        cellTable.setColumnWidth(0, 400, com.google.gwt.dom.client.Style.Unit.PX);
        listDataProvider = new ListDataProvider<>();
        listDataProvider.addDataDisplay(cellTable);
    }

    public FileViewImpl() {
        cellTable = new CellTable<>();
        buildTable();
        initWidget(ourUiBinder.createAndBindUi(this));
    }


}