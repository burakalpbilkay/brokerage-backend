package com.brokaragefirm.api.Error;

public class Errors {
  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String m) {
      super(m);
    }
  }

  public static class BadRequestException extends RuntimeException {
    public BadRequestException(String m) {
      super(m);
    }
  }

  public static class ForbiddenException extends RuntimeException {
    public ForbiddenException(String m) {
      super(m);
    }
  }

  public static class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String m) {
      super(m);
    }
  }
}
