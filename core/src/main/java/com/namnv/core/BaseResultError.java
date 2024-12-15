package com.namnv.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResultError extends BaseResult {

  public static final BaseResultError COMMAND_NOT_FOUND = new BaseResultError("Command not found");
  private String message;

  @Override
  public String toString() {
    return String.format("%s::%s", 500, message);
  }
}
