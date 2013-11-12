package com.iha.wcc.exception;

/**
 * Created by Ambroise on 12/11/13.
 * Not used yet, maybe never.
 */
public class MessageException extends Exception{
    private String message;
    private MessageException(){};

    public MessageException(String message){
        this.message = message;
    }
}
