package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import server.ClientHandler;
import server.Server;
import ui.main.ServerUI;
import ui.main.ServerUINodesManager;

import java.util.List;

public class ControllerServer implements ControlledNode {

    @FXML private ListView<Integer> mListViewClients;

    private ServerUINodesManager mServerUINodesManager;
    private Server mServer;
    private ObservableMap<Integer, ClientHandler> mObservableMapClients;
    private ObservableList<Integer> mObservableListClients;

    @Override
    public void init() {
        mServer = mServerUINodesManager.getServer();
        mObservableMapClients = mServer.getObservableMapClients();
        mObservableListClients = FXCollections.observableArrayList();

        mObservableMapClients.addListener(new MapChangeListener<Integer, ClientHandler>() {
            @Override
            public void onChanged(Change<? extends Integer, ? extends ClientHandler> change) {
                Integer key = change.getKey();
                if (change.wasAdded()) {
                    mObservableListClients.add(key);
                } else if (change.wasRemoved()) {
                    mObservableListClients.remove(key);
                }
            }
        });

        mListViewClients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mListViewClients.setItems(mObservableListClients);
    }

    @Override
    public void setNodesManager(ServerUINodesManager serverUINodesManager) {
        mServerUINodesManager = serverUINodesManager;
    }

    @FXML
    public void handleButtonDisconnect() {
        MultipleSelectionModel multipleSelectionModel = mListViewClients.getSelectionModel();
        if (multipleSelectionModel.isEmpty()) {
            return;
        }

        List<Integer> clientIDs = multipleSelectionModel.getSelectedItems();
        while (clientIDs.get(0) != null) {
            mObservableMapClients.remove(clientIDs.get(0));
        }
    }

    @FXML
    public void handleButtonDisconnectAll() {
        if (mObservableMapClients.isEmpty())
            return;

        for (Integer clientID : mObservableListClients) {
            mObservableMapClients.get(clientID).shutDown();
        }

        mObservableMapClients.clear();
    }

    @FXML
    public void handleButtonStopServer() {
        if (mServer.isRunning()) {
            mServer.shutdown();
        }
        mServerUINodesManager.getPrimaryStage().setTitle("Server");
        mServerUINodesManager.setNode(ServerUI.LAYOUT_INPUT, ServerUINodesManager.SlideAnimationDirection.SLIDE_RIGHT);
    }
}
