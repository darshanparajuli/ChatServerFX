package ui.main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import server.Server;

public class ServerUI extends Application {

    public static final String LAYOUT_INPUT = "LAYOUT_INPUT";
    public static final String LAYOUT_INPUT_PATH = "../layouts/Input.fxml";

    public static final String LAYOUT_SERVER = "LAYOUT_SERVER";
    public static final String LAYOUT_SERVER_PATH = "../layouts/Server.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Server");

        final Server server = new Server();

        ServerUINodesManager serverUINodesManager = new ServerUINodesManager(primaryStage, server);
        serverUINodesManager.loadNode(LAYOUT_INPUT, LAYOUT_INPUT_PATH);
        serverUINodesManager.loadNode(LAYOUT_SERVER, LAYOUT_SERVER_PATH);
        serverUINodesManager.setNode(LAYOUT_INPUT, ServerUINodesManager.SlideAnimationDirection.SLIDE_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(serverUINodesManager);

        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server.shutdown();
                    }
                }).start();
            }
        });
    }

    public static void main(String... args) {
        Application.launch(args);
    }
}
