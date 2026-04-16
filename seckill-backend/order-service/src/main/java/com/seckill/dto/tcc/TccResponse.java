package com.seckill.dto.tcc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TccResponse {

    private boolean success;

    private String message;

    public static TccResponse ok(String msg) {
        return new TccResponse(true, msg);
    }

    public static TccResponse fail(String msg) {
        return new TccResponse(false, msg);
    }
}


