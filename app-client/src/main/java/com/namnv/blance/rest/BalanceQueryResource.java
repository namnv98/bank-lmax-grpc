package com.namnv.blance.rest;

import com.namnv.blance.core.RequestBufferDispatcher;
import com.namnv.blance.query.BalanceDetailQuery;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/balance/query/{id}")
@RequiredArgsConstructor
public class BalanceQueryResource {

  private final RequestBufferDispatcher<BaseRequest> requestBufferDispatcher;

  @GetMapping
  public CompletableFuture<BaseResponse> currentBalance(@PathVariable("id") Long id) {
    return requestBufferDispatcher.dispatch(new BalanceDetailQuery(id));
  }
}
