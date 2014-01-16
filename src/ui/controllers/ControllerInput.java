package ui.controllers;

import Utils.Console;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import server.Server;
import ui.main.ServerUI;
import ui.main.ServerUINodesManager;

public class ControllerInput implements ControlledNode {

    private ServerUINodesManager mServerUINodesManager;

    @FXML private TextField mTextFieldInput;

    private Server mServer;

    @Override
    public void init() {
        mServer = mServerUINodesManager.getServer();
    }

    @Override
    public void setNodesManager(ServerUINodesManager serverUINodesManager) {
        mServerUINodesManager = serverUINodesManager;
    }

    @FXML
    public void handleButtonOK() {
        if (!mServer.isRunning())
            initServerUI();
    }

    @FXML
    public void handleButtonCancel() {
        mServerUINodesManager.getPrimaryStage().close();
    }

    @FXML
    public void handleTextFieldInput() {
        initServerUI();
    }

    private void initServerUI() {
        String port = mTextFieldInput.getText();
        try {
            mServer.init(Integer.parseInt(port));
        } catch (NumberFormatException e) {
            Console.error(e.getMessage());
            if (port.trim().toLowerCase().equals("any"))
                mServer.init(0);
            else
                return;
        }
        mServerUINodesManager.getPrimaryStage().setTitle("Server  |  " + mServer.getPort());
        mServerUINodesManager.setNode(ServerUI.LAYOUT_SERVER, ServerUINodesManager.SlideAnimationDirection.SLIDE_LEFT);
    }

}
