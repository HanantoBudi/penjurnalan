package id.co.askrindo.penjurnalan.errors.exception;

public class InternalServerErrorException extends RuntimeException {
    String message;

    public InternalServerErrorException() {
        super();
    }

    public InternalServerErrorException(String message) {
        super(message);
        this.message = message;
    }
}