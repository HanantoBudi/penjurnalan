package id.co.askrindo.penjurnalan.errors.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import id.co.askrindo.penjurnalan.errors.exception.RecordNotFoundException;
import id.co.askrindo.penjurnalan.errors.exception.InternalServerErrorException;

@ControllerAdvice
public class GlobalErrorHandler {

    @Getter
    @Setter
    @AllArgsConstructor
    @RequiredArgsConstructor
    public class ResponseError {
        private String errorMessage;
        private int statusCode;
    }

    @ExceptionHandler(RecordNotFoundException.class)
    @ResponseBody
    public ResponseError handleCustomException(RecordNotFoundException ex) {
        ResponseError responseError = new ResponseError();
        responseError.setErrorMessage(ex.getMessage());
        responseError.setStatusCode(HttpStatus.NOT_FOUND.value());
        return responseError;

    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseBody
    public ResponseError handleInternalServerError(InternalServerErrorException ex) {
        ResponseError responseError = new ResponseError();
        responseError.setErrorMessage(ex.getMessage());
        responseError.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return responseError;

    }
}
