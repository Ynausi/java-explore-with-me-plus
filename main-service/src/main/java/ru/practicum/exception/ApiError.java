package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String message;
    private String reason;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime timestamp;
    private List<ErrorDetail> errors;
}