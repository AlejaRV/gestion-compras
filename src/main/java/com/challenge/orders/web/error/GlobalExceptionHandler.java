package com.challenge.orders.web.error;

import com.challenge.orders.domain.OrderStatus;
import com.challenge.orders.domain.exception.DeliveredOrderCancellationException;
import com.challenge.orders.domain.exception.DomainException;
import com.challenge.orders.domain.exception.InvalidStatusTransitionException;
import com.challenge.orders.domain.exception.OrderNotFoundException;
import com.challenge.orders.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Traduce excepciones a respuestas RFC 7807 (application/problem+json) homogeneas:
 * <pre>
 * { "type", "title", "status", "detail", "instance", "code", "timestamp", "correlationId", "errors"? }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = statusFor(ex);
        log.warn("event=business_rule_violation code={} status={} detail=\"{}\"",
                ex.getRuleCode(), status.value(), ex.getMessage());
        return build(status, titleFor(status), ex.getMessage(), ex.getRuleCode(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", String.valueOf(fe.getDefaultMessage())))
                .toList();
        log.warn("event=validation_error errors={}", errors);
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida",
                "La solicitud contiene datos invalidos", "VALIDATION_ERROR", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        log.warn("event=malformed_request detail=\"{}\"", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida",
                "El cuerpo de la solicitud es invalido o contiene valores no permitidos. Estados validos: "
                        + Arrays.toString(OrderStatus.values()),
                "MALFORMED_REQUEST", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida",
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue(),
                "INVALID_PARAMETER", request, null);
    }

    /**
     * Excepciones propias de Spring MVC que ya conocen su status (404 recurso inexistente,
     * 405 metodo no permitido, 415 media type...) se respetan; cualquier otra es un 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            log.warn("event=http_error status={} detail=\"{}\"", status.value(), ex.getMessage());
            return build(status, status.getReasonPhrase(), errorResponse.getBody().getDetail(),
                    "HTTP_" + status.value(), request, null);
        }
        log.error("event=unexpected_error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrio un error inesperado. Use el correlationId para rastrearlo.",
                "INTERNAL_ERROR", request, null);
    }

    static HttpStatus statusFor(DomainException ex) {
        if (ex instanceof OrderNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof DeliveredOrderCancellationException || ex instanceof InvalidStatusTransitionException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private static String titleFor(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Recurso no encontrado";
            case CONFLICT -> "Conflicto de estado";
            default -> "Regla de negocio incumplida";
        };
    }

    private static ResponseEntity<ProblemDetail> build(HttpStatus status, String title, String detail, String code,
                                                       HttpServletRequest request, Object errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        if (errors != null) {
            problem.setProperty("errors", errors);
        }
        return ResponseEntity.status(status).body(problem);
    }
}
