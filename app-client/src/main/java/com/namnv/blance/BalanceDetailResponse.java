package com.namnv.blance;

import com.namnv.blance.rest.BaseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BalanceDetailResponse extends BaseResponse {
  private long id;
  private long amount;
}
