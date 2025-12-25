package com.clinassist.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception Unit Tests")
class ExceptionTest {

    @Test
    void resourceNotFoundException_ShouldContainMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        assertThat(ex.getMessage()).isEqualTo("User not found");
    }

    @Test
    void badRequestException_ShouldContainMessage() {
        BadRequestException ex = new BadRequestException("Invalid data");
        assertThat(ex.getMessage()).isEqualTo("Invalid data");
    }

    @Test
    void globalExceptionHandler_ShouldHandleResourceNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
    }

    @Test
    void globalExceptionHandler_ShouldHandleGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("Internal error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }
}
