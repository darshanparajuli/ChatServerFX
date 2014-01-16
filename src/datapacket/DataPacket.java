package datapacket;

import java.io.Serializable;

public class DataPacket implements Serializable {

    private static final long serialVersionUID = 142741178343251564L;

    public enum RequestType {
        NONE, CONNECT_SERVER, DISCONNECT_SERVER, PING, CONNECT_CLIENT, DISCONNECT_CLIENT;
    }

    private int mToClientID;
    private int mFromClientID;

    private String mMessage;

    private RequestType mRequestType;

    public DataPacket() {
        mRequestType = RequestType.NONE;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public int getFromClientID() {
        return mFromClientID;
    }

    public void setFromClientID(int fromClientID) {
        mFromClientID = fromClientID;
    }

    public int getToClientID() {
        return mToClientID;
    }

    public void setToClientID(int toClientID) {
        mToClientID = toClientID;
    }

    public void setRequestType(RequestType requestType) {
        mRequestType = requestType;
    }

    public RequestType getRequestType() {
        return mRequestType;
    }
}
