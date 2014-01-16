package server;

import Utils.Console;
import datapacket.DataPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

    private static final int BACKLOG_QUEUE_SIZE = 500;

    private ServerSocket mServerSocket;
    private Socket mSocket;
    private int mPort;
    private ObjectInputStream mObjectInputStream;
    private ObjectOutputStream mObjectOutputStream;

    private Thread mMainThread;
    private volatile boolean mRunning;
    private ExecutorService mExecutorService;

    private Map<Integer, ClientHandler> mMapClients;
    private ObservableMap<Integer, ClientHandler> mObservableMapClients;

    private DataPacket mDataPacketIn;

    public Server() {
        mExecutorService = Executors.newCachedThreadPool();
        mMapClients = new HashMap<>();
        mObservableMapClients = FXCollections.observableMap(mMapClients);
    }

    public void init(int port) {
        try {
            mServerSocket = new ServerSocket();
            mServerSocket.bind(new InetSocketAddress(port), BACKLOG_QUEUE_SIZE);
        } catch (BindException e) {
            Console.error(e.getMessage());
            try {
                mServerSocket.bind(new InetSocketAddress(0), BACKLOG_QUEUE_SIZE);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPort = mServerSocket.getLocalPort();
        mRunning = true;
        mMainThread = new Thread(this);
        mMainThread.start();
    }

    @Override
    public void run() {
        Console.info("server has started on port " + mPort);
        while (mRunning) {
            try {
                mSocket = mServerSocket.accept();

                if (!mRunning)
                    break;

                mObjectInputStream = new ObjectInputStream(new BufferedInputStream(mSocket.getInputStream()));
                mDataPacketIn = (DataPacket) mObjectInputStream.readObject();

                switch (mDataPacketIn.getRequestType()) {
                    case NONE:
                        // don't do anything...
                        Console.info("request type: none, from client: " + mDataPacketIn.getFromClientID());
                        break;
                    case PING:
                        handleRequestPing();
                        break;
                    case CONNECT_SERVER:
                        handleRequestConnectServer();
                        break;
                    case CONNECT_CLIENT:
                        handleRequestConnectClient();
                        break;
                    case DISCONNECT_CLIENT:
                        handleRequestDisconnectClient();
                        break;
                    case DISCONNECT_SERVER:
                        handleDisconnectServer();
                        break;
                }

            } catch (ClassNotFoundException | IOException e) {
                Console.error(e.getMessage());
            }
        }
        Console.info("server has shut down");
    }

    private void handleRequestPing() throws IOException {
        Console.info("request type: ping, from client: " + mDataPacketIn.getFromClientID());
        mObjectOutputStream = new ObjectOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
        DataPacket dataPacketOut = new DataPacket();
        dataPacketOut.setRequestType(DataPacket.RequestType.PING);
        dataPacketOut.setMessage("Hi Client " + mDataPacketIn.getFromClientID() + ", this is the server!");
        mObjectOutputStream.writeObject(dataPacketOut);
        mObjectOutputStream.flush();
    }

    private void handleRequestConnectServer() {
        int fromClientID = mDataPacketIn.getFromClientID();
        Console.info("request type: connect to Server, from client: " + fromClientID);
        if (!mObservableMapClients.containsKey(fromClientID)) {
            addClient(fromClientID);
        } else {
            Console.error(fromClientID + " already exists");
        }
    }

    private void handleRequestConnectClient() {
        int fromClientID = mDataPacketIn.getFromClientID();
        int toClientID = mDataPacketIn.getToClientID();
        Console.info("request type: connect to Client, from client: " + fromClientID + ", to client: " + toClientID);
        if (mObservableMapClients.containsKey(fromClientID)) {
            if (mObservableMapClients.containsKey(toClientID)) {
                mObservableMapClients.get(toClientID).setSocketOut(mObservableMapClients.get(fromClientID).getSocketIn());
                mObservableMapClients.get(fromClientID).setSocketOut(mObservableMapClients.get(toClientID).getSocketIn());
                mExecutorService.execute(mObservableMapClients.get(fromClientID));
                mExecutorService.execute(mObservableMapClients.get(toClientID));
            } else {
                Console.error(toClientID + " not available");
            }
        } else {
            Console.error(fromClientID + " not available");
        }
    }

    private void handleRequestDisconnectClient() {
        int fromClientID = mDataPacketIn.getFromClientID();
        int toClientID = mDataPacketIn.getToClientID();
        Console.info("request type: disconnect from Client, from client: " + fromClientID + ", to client: " + toClientID);
        if (mObservableMapClients.containsKey(fromClientID)) {
            if (mObservableMapClients.containsKey(toClientID)) {
                mObservableMapClients.get(toClientID).shutDown();
                mObservableMapClients.get(fromClientID).shutDown();
            } else {
                Console.error(toClientID + " not available");
            }
        } else {
            Console.error(fromClientID + " not available");
        }
    }

    private void handleDisconnectServer() {
        int fromClientID = mDataPacketIn.getFromClientID();
        int toClientID = mDataPacketIn.getToClientID();
        Console.info("request type: disconnect from Server, from client: " + fromClientID);
        if (mObservableMapClients.containsKey(fromClientID)) {
            if (mObservableMapClients.get(fromClientID).isRunning()) {
                mObservableMapClients.get(fromClientID).shutDown();
            }
            removeClient(fromClientID);
        } else {
            Console.error(fromClientID + " not available");
        }

    }

    public void shutdown() {
        mRunning = false;
        Set<Integer> keySet = mObservableMapClients.keySet();
        for (Integer key : keySet) {
            mObservableMapClients.get(key).shutDown();
        }
        synchronized (mObservableMapClients) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    mObservableMapClients.clear();
                }
            });
        }
        try {
            if (mObjectInputStream != null) mObjectInputStream.close();
            if (mObjectOutputStream != null) mObjectOutputStream.close();
            if (mServerSocket != null) mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mExecutorService.shutdown();
        try {
            if (!mExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                mExecutorService.shutdownNow();
                if (!mExecutorService.awaitTermination(5, TimeUnit.SECONDS))
                    Console.error("ExecutorService did not terminate");
            }
        } catch (InterruptedException e) {
            mExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        Console.info("server shutting down...");
    }

    private void addClient(final int fromClientID) {
        final ClientHandler clientHandler = new ClientHandler(mSocket);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                synchronized (mObservableMapClients) {
                    mObservableMapClients.put(fromClientID, clientHandler);
                    Console.info(fromClientID + " has connected");
                }
            }
        });
    }

    private void removeClient(final int fromClientID) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                synchronized (mObservableMapClients) {
                    mObservableMapClients.remove(fromClientID);
                    Console.info(fromClientID + " has disconnected");
                }
            }
        });
    }

    public int getPort() {
        return mPort;
    }

    public ObservableMap<Integer, ClientHandler> getObservableMapClients() {
        return mObservableMapClients;
    }

    public boolean isRunning() {
        return mRunning;
    }
}
