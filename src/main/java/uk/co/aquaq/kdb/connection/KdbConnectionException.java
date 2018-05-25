package uk.co.aquaq.kdb.connection;

class KdbConnectionException extends RuntimeException {

    KdbConnectionException(String msg) {
        super(msg);
    }

    KdbConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}