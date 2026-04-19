package org.ringling.backend.common.controller;

import froggy.winterframework.http.ResponseEntity;
import froggy.winterframework.validation.BindingResult;
import froggy.winterframework.validation.MethodArgumentNotValidException;
import froggy.winterframework.web.bind.annotation.ExceptionHandler;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.dto.ErrorDetail;
import org.ringling.backend.common.dto.ErrorResponse;
import org.ringling.backend.common.dto.ValidationErrorDetail;
import org.ringling.backend.common.exception.CustomException;

@Slf4j
public abstract class BaseApiController {

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.of(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
            .body(ErrorResponse.of(
                ErrorCode.INVALID_REQUEST.getCode(),
                ErrorCode.INVALID_REQUEST.getMessage(),
                createValidationErrors(exception.getBindingResult())
            ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception in controller", exception);

        return ResponseEntity.status(ErrorCode.UNEXPECTED_ERROR.getHttpStatus())
            .body(ErrorResponse.of(
                ErrorCode.UNEXPECTED_ERROR.getCode(),
                ErrorCode.UNEXPECTED_ERROR.getMessage()
            ));
    }

    private List<ErrorDetail> createValidationErrors(BindingResult bindingResult) {
        List<ErrorDetail> errors = new ArrayList<>();

        for (BindingResult.FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.add(new ValidationErrorDetail(
                "field",
                fieldError.getCode(),
                fieldError.getMessage(),
                fieldError.getField(),
                fieldError.getRejectedValue()
            ));
        }

        for (BindingResult.ObjectError globalError : bindingResult.getGlobalErrors()) {
            errors.add(new ValidationErrorDetail(
                "global",
                globalError.getCode(),
                globalError.getMessage(),
                null,
                null
            ));
        }

        return errors;
    }
}
