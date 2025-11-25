package com.slowcoder.annotation.dto;

import lombok.Data;

@Data
public class RejectReq {
    Long recordId;
    String reason;
}
