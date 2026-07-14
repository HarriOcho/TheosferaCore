package com.theosfera.core.network.auth;

public enum PlayerAuthenticationRequestStatus {
    SUBMITTED,
    ALREADY_PENDING,
    NOT_AUTHENTICATION_BACKEND,
    TRANSPORT_UNAVAILABLE,
    SERVICE_CLOSED
}
