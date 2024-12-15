package com.namnv.blance.core;

import com.namnv.blance.rest.BaseRequest;
import com.namnv.blance.rest.BaseResponse;
import java.util.concurrent.CompletableFuture;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestBufferEvent {
  private BaseRequest request;
  private CompletableFuture<BaseResponse> responseFuture;
}
