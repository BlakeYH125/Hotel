package org.hotel.controller;

import org.hibernate.PessimisticLockException;
import org.hibernate.exception.LockTimeoutException;
import org.hotel.model.exceptions.RoomNotOccupiedException;
import org.hotel.model.exceptions.RoomAlreadyExistsException;
import org.hotel.model.exceptions.RoomNotFoundException;
import org.hotel.model.exceptions.RoomAlreadyInServiceException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.model.exceptions.ServiceAlreadyExistsException;
import org.hotel.model.exceptions.GuestNotFoundException;
import org.hotel.model.exceptions.GuestAlreadyExistsException;
import org.hotel.model.exceptions.GuestAlreadyEvictedException;
import org.hotel.model.exceptions.UserAlreadyExistsException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.model.exceptions.WrongPasswordException;
import org.hotel.model.exceptions.RoomAlreadyOccupiedException;
import org.hotel.model.exceptions.RoomAlreadyAvailableException;
import org.hotel.model.exceptions.ChangeStatusBannedException;
import org.hotel.model.exceptions.UserNotFoundException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message, Exception e) {
        Map<String, String> response = new HashMap<>();
        String errorMessage;
        if (message != null) {
            errorMessage = message;
        } else {
            errorMessage = e.getClass().getSimpleName();
        }
        response.put("error", errorMessage);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({RoomNotFoundException.class, GuestNotFoundException.class, ServiceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFountExceptions(RuntimeException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler({RoomAlreadyOccupiedException.class,
            RoomAlreadyInServiceException.class,
            RoomAlreadyAvailableException.class,
            RoomAlreadyExistsException.class,
            ServiceAlreadyExistsException.class,
            RoomNotOccupiedException.class,
            GuestAlreadyExistsException.class,
            GuestAlreadyEvictedException.class})
    public ResponseEntity<Map<String, String>> handleConflictExceptions(RuntimeException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage(), e);
    }

    @ExceptionHandler({WrongSortTypeException.class, ChangeStatusBannedException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequestExceptions(RuntimeException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, "Указанный URL-адрес не существует: " + e.getResourcePath(), e);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "У вас нет прав на выполнение данной операции");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Метод " + e.getMethod() + " не поддерживается для этого адреса.", e);
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<Map<String, String>> handleWrongPassword(WrongPasswordException e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException e) {
        return buildResponse(HttpStatus.CONFLICT, "Ошибка при регистрации: пользователь уже существует", e);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String errorMessage = "Отсутствует параметр " + ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Тело запроса отсутствует или содержит некорректный JSON", e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Некорректное значение параметра: " + e.getName(), e);
    }

    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            PessimisticLockException.class,
            LockTimeoutException.class
    })
    public ResponseEntity<Map<String, String>> handlePessimisticLock(Exception e) {
        return buildResponse(HttpStatus.CONFLICT, "Не удалось получить блокировку данных. Повторите запрос", e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherExceptions(Exception e) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутрення ошибка", e);
    }
}
