package cz.mycom.veeam.portal.controller;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

@ControllerAdvice
@Slf4j
public class ExceptionController {


    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorInfo notFound(HttpServletRequest request, SQLException e) {
        log.warn("SQLException on " + request.getRequestURL() + ": " + e.getMessage(), e);
        return new ErrorInfo(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorInfo accessDenied(HttpServletRequest request, Exception e) {
        log.debug("Access denied on " + request.getRequestURL() + ": " + e.getMessage(), e);
        return new ErrorInfo(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo defaultErrorHandler(HttpServletRequest request, Exception e) {
        log.error(request.getRequestURL() + " throws exception " + e.getClass().getName() + ": " + e.getMessage(), e);

        Throwable cause = e.getCause();
        if (cause != null) {
            log.error(cause.getClass().getName() + ": " + cause.getMessage());
            if (cause instanceof JDBCException) {
                JDBCException jdbcException = (JDBCException) cause;
                SQLException sqlException = jdbcException.getSQLException();
                return new ErrorInfo("Chyba: " + sqlException.getMessage());
            }
            if (cause instanceof RollbackException && cause.getCause() != null) {
                log.error(cause.getCause().getClass().getName() + ": " + cause.getCause().getMessage());
                return new ErrorInfo(cause.getCause().getMessage());
            }

        }
        return new ErrorInfo(e.getMessage());
    }

    public static class ErrorInfo {
        public String message;

        public ErrorInfo(String message) {
            this.message = message;
        }
    }
}
