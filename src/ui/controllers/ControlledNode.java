package ui.controllers;

import ui.main.ServerUINodesManager;

public interface ControlledNode {

    public void init();

    public void setNodesManager(ServerUINodesManager serverUINodesManager);

}
