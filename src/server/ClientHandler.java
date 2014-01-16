package server;

import Utils.Console;
import datapacket.DataPacket;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket mSocketIn;
    private volatile boolean mRunning;
    private ObjectInputStream mObjectInputStream;
    private ObjectOutputStream mObjectOutputStream;

    private Socket mSocketOut;

    public ClientHandler(Socket socket) {
        mSocketIn = socket;
        mRunning = true;
    }

    @Override
    public void run() {
        while (mRunning) {
            try {
                mObjectInputStream = new ObjectInputStream(new BufferedInputStream(mSocketIn.getInputStream()));
                DataPacket dataPacket = (DataPacket) mObjectInputStream.readObject();

                if (mSocketOut != null) {
                    mObjectOutputStream = new ObjectOutputStream(new BufferedOutputStream(mSocketOut.getOutputStream()));
                    mObjectOutputStream.writeObject(dataPacket);
                    mObjectOutputStream.flush();
                }

            } catch (EOFException e) {
                Console.error(e.getMessage());
            } catch (IOException e) {
                Console.error(e.getMessage());
                shutDown();
            } catch (ClassNotFoundException e) {
                Console.error(e.getMessage());
            }
        }
        Console.info("client handler has shut down");
    }

    public void shutDown() {
        Utils.Console.info("shutting down client handler...");
        mRunning = false;
        try {
            if (mObjectInputStream != null)
                mObjectInputStream.close();
            if (mObjectOutputStream != null)
                mObjectOutputStream.close();
            if (mSocketIn != null)
                mSocketIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!mRunning) {
            Console.info("client handler has shut down");
        }
    }

    public Socket getSocketIn() {
        return mSocketIn;
    }

    public void setSocketOut(Socket socketOut) {
        mSocketOut = socketOut;
    }

    public Socket getSocketOut() {
        return mSocketOut;
    }

    public boolean isRunning() {
        return mRunning;
    }
}
