package com.namnv.blance.rest;

import com.namnv.blance.command.CreateBalanceCommand;
import com.namnv.blance.command.DepositCommand;
import com.namnv.blance.command.TransferCommand;
import com.namnv.blance.command.WithdrawCommand;
import com.namnv.blance.core.RequestBufferDispatcher;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/balance/command")
@RequiredArgsConstructor
public class BalanceCommandResource {

  private final RequestBufferDispatcher<BaseRequest> requestBufferDispatcher;

  @GetMapping("/create")
  public CompletableFuture<BaseResponse> create() {
    CreateBalanceCommand command = new CreateBalanceCommand();
    return requestBufferDispatcher.dispatch(command);
  }

  @PostMapping("/deposit")
  public CompletableFuture<BaseResponse> deposit(@RequestBody DepositCommand command) {
    return requestBufferDispatcher.dispatch(command);
  }

  @PostMapping("/withdraw")
  public CompletableFuture<BaseResponse> withdraw(@RequestBody WithdrawCommand command) {
    return requestBufferDispatcher.dispatch(command);
  }

  @PostMapping("/transfer")
  public CompletableFuture<BaseResponse> transfer(@RequestBody TransferCommand command) {
    return requestBufferDispatcher.dispatch(command);
  }
}
