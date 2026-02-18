package com.resale.loveresalecommunication.shared;

import com.resale.loveresalecommunication.Exception.PermissionDeniedException;
import com.resale.loveresalecommunication.utils.ReturnObject;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PermissionDeniedDataAccessException.class)
    public ResponseEntity<ReturnObject<Void>> handlePermissionDenied(PermissionDeniedException ex) {
        ReturnObject<Void> response = new ReturnObject<>();
        response.setMessage(ex.getMessage());
        response.setStatus(false);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}


