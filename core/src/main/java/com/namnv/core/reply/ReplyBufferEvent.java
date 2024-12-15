package com.namnv.core.reply;

import com.namnv.core.BaseResult;
import com.namnv.core.BufferEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyBufferEvent implements BufferEvent {
  private String replyChannel;
  private String correlationId;
  private BaseResult result;
}
