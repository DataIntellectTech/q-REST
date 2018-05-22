package uk.co.aquaq.kdb.controller;

class InvalidRequestException extends RuntimeException {

    InvalidRequestException(String message) {
        super(message);
    }
}
