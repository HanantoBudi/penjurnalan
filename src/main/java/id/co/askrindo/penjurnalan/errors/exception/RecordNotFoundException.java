package id.co.askrindo.penjurnalan.errors.exception;

public class RecordNotFoundException extends RuntimeException {
    String message;

    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(String message) {
        super(message);
        this.message = message;
    }
}