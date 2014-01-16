package ui.main;

import Utils.Console;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.Server;
import ui.controllers.ControlledNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerUINodesManager extends StackPane {

    private static final Duration ANIMATION_DURATION = Duration.millis(500);

    public static enum SlideAnimationDirection {
        SLIDE_LEFT, SLIDE_RIGHT
    }

    private Map<String, Node> mNodes;
    private Stage mPrimaryStage;
    private Server mServer;

    public ServerUINodesManager(Stage primarStage, Server server) {
        mPrimaryStage = primarStage;
        mServer = server;
        mNodes = new HashMap<String, Node>();
    }

    public boolean loadNode(String name, String layout) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(layout));
            fxmlLoader.load();
            Node node = fxmlLoader.getRoot();
            ControlledNode controlledNode = fxmlLoader.getController();
            controlledNode.setNodesManager(this);
            controlledNode.init();
            mNodes.put(name, node);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setNode(final String name, SlideAnimationDirection slideAnimationDirection) {
        if (mNodes.get(name) != null) {
            if (!getChildren().isEmpty()) {
                final Node currNode = getChildren().get(getChildren().size() - 1);
                final Node newNode = mNodes.get(name);
                final double width = getWidth();
                double currNodeStartX, currNodeEndX, newNodeStartX, newNodeEndX;

                if (slideAnimationDirection == SlideAnimationDirection.SLIDE_LEFT) {
                    currNodeStartX = currNode.getLayoutX();
                    currNodeEndX = currNodeStartX - width;
                    newNodeStartX = newNode.getLayoutX() + width;
                    newNodeEndX = newNode.getLayoutX();
                } else {
                    currNodeStartX = currNode.getLayoutX();
                    currNodeEndX = currNodeStartX + width;
                    newNodeStartX = newNode.getLayoutX() - width;
                    newNodeEndX = newNode.getLayoutX();
                }

                Transition slideOutTransition = TranslateTransitionBuilder.create().node(currNode).fromX(currNodeStartX).toX(currNodeEndX).duration(ANIMATION_DURATION)
                        .interpolator(Interpolator.EASE_IN).cycleCount(1).build();
                slideOutTransition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        getChildren().remove(currNode);
                    }
                });
                getChildren().add(0, newNode);
                Transition slideInTransition = TranslateTransitionBuilder.create().node(newNode).fromX(newNodeStartX).toX(newNodeEndX).duration(ANIMATION_DURATION)
                        .interpolator(Interpolator.EASE_OUT).cycleCount(1).build();
                slideOutTransition.play();
                slideInTransition.play();
            } else {
                setOpacity(0.0);
                getChildren().add(mNodes.get(name));
                Transition fadeTransition = FadeTransitionBuilder.create().node(this).fromValue(0.0).toValue(1.0).duration(Duration.millis(500))
                        .interpolator(Interpolator.EASE_OUT).cycleCount(1)
                        .build();
                fadeTransition.play();
            }
            return true;
        } else {
            Console.error("Node not loaded yet.");
        }
        return false;
    }

    public void unLoadNode(String name) {
        if (mNodes.remove(name) == null) {
            Console.error(name + " node didn't exist.");
        } else {
            Console.info(name + " node removed from the stack.");
        }
    }

    public Stage getPrimaryStage() {
        return mPrimaryStage;
    }

    public Server getServer() {
        return mServer;
    }
}