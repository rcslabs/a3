package com.rcslabs.redis;

/**
 * Created by ykrkn on 15.10.14.
 */
public class MessagingException extends Exception {

    public static final int SERIALIZATION_ERROR = 1;

    public static final int DESERIALIZATION_ERROR = 2;

    public static final int SERIALIZER_EXIST = 3;

    public static final int SERIALIZER_ABSENT = 4;

    private final int errorCode;

    private Object[] args;

    public MessagingException(int code, Object ... args) {
        this(code);
        this.args = args;
    }

    public MessagingException(int code) {
        super();
        errorCode = code;
    }

    public int getErrorCode(){
        return errorCode;
    }

    @Override
    public String getMessage() {
        switch(errorCode){
            case SERIALIZATION_ERROR: return "Serialization error";
            case DESERIALIZATION_ERROR: return "Deserialization error";
            case SERIALIZER_EXIST: return "Serializer exist" + (1 == args.length ? " for channel " + args[0] : "");
            case SERIALIZER_ABSENT: return "Serializer absent" + (1 == args.length ? " for channel " + args[0] : "");
            default: return "Error code " + errorCode;
        }
    }
}
