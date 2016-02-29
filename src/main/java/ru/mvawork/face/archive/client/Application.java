package ru.mvawork.face.archive.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.xml.client.*;
import ru.mvawork.face.archive.client.view.FileView;
import ru.mvawork.face.archive.shared.FileInfo;


public class Application extends Composite implements FormPanel.SubmitCompleteHandler, RequestCallback, ValueChangeHandler<String> {


    @SuppressWarnings("WeakerAccess")
    interface ApplicatonUiBinder extends UiBinder<HTMLPanel, Application> {
    }

    private static ApplicatonUiBinder ourUiBinder = GWT.create(ApplicatonUiBinder.class);

    @UiField
    Button loadButton;
    @UiField(provided = true)
    FormPanel loadFormPanel;
    @UiField
    FileUpload fileUpload;
    @UiField
    SimplePanel cellTablePanel;

    private FileView fileView = GWT.create(FileView.class);


    private void createLoadForm() {
        loadFormPanel = new FormPanel();
        loadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        loadFormPanel.setMethod(FormPanel.METHOD_POST);
        loadFormPanel.setAction(GWT.getHostPageBaseURL() + "rest/archive/load");
        loadFormPanel.addSubmitCompleteHandler(this);
    }

    private void createCellTable(FileInfo fileInfo) {
        fileView.setFileInfo(uuid, fileInfo);
        cellTablePanel.setWidget(fileView);
        History.newItem(fileInfo.getName());
        History.fireCurrentHistoryState();
    }


    public Application() {
        createLoadForm();
        initWidget(ourUiBinder.createAndBindUi(this));
        History.addValueChangeHandler(this);
        History.newItem("");
     }

    @Override
    public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
        createFileList(event.getResults());
    }

    private void fillFileInfoList(FileInfo rootFileInfo, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            NamedNodeMap nodeMap = node.getAttributes();
            Node n = nodeMap.getNamedItem("name");
            Node d = nodeMap.getNamedItem("isDirectory");
            if (d.getNodeValue().equals("true")) {
                fillFileInfoList(rootFileInfo.addSubDir(n.getNodeValue()), node.getChildNodes());
            } else {
                rootFileInfo.addFile(n.getNodeValue());
            }
        }
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
            Document document = XMLParser.parse(response.getText());
            Element element = document.getDocumentElement();
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeName().equals("FileInfo")) {
                    NamedNodeMap nodeMap = node.getAttributes();
                    Node n = nodeMap.getNamedItem("name");
                    FileInfo fileInfo = new FileInfo(null, n.getNodeValue(), true);
                    fillFileInfoList(fileInfo, node.getChildNodes());
                    createCellTable(fileInfo);
                    break;
                }
            }
        }
    }

    @Override
    public void onError(Request request, Throwable exception) {
        Window.alert("not Ok");
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("loadButton")
    void loadButtonClick(ClickEvent event) {
        loadFormPanel.submit();
    }

    private String uuid;

    private void createFileList(String uuid) {
        this.uuid = uuid;
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "rest/archive/getFileList?uuid=" + URL.encodeQueryString(uuid));
        rb.setCallback(this);
        try {
            rb.send();
        } catch (RequestException ignored) {

        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        if (fileView != null) {
            fileView.switchDirectory(URL.decodeQueryString(event.getValue()));
        }
    }

}