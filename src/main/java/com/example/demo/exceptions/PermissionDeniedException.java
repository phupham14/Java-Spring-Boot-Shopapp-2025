package com.example.demo.exceptions;

public class PermissionDeniedException extends Exception {
  public PermissionDeniedException(String message) {
    super(message);
  }
}
