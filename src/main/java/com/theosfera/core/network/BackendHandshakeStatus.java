package com.theosfera.core.network;

public enum BackendHandshakeStatus {
    WAITING_FOR_CARRIER,
    HELLO_PENDING,
    AUTHORIZED,
    REJECTED,
    CLOSED
}
