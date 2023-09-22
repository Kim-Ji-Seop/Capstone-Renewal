package com.uou.capstone.domain.app.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLoginEmailReq {
    private String email;
    private String password;
}
