package com.gym.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatVO {

    private String role;
    private String content;
}
