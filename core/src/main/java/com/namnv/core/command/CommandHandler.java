package com.namnv.core.command;

import com.namnv.core.BaseCommand;
import com.namnv.core.BaseResult;

public interface CommandHandler {

  BaseResult onCommand(BaseCommand command);
}
