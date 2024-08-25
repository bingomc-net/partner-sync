package net.bingomc.partnersync.exception;

public class OutsideQuotaException extends RuntimeException {
    public OutsideQuotaException(String message) {
        super(message);
    }
}
