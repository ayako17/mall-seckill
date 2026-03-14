package com.seckill.controller;

import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.service.UserService;
import com.seckill.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册（带详细错误信息）
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO, 
                                      BindingResult bindingResult) {
        log.info("收到注册请求: {}", registerDTO.getUsername());
        
        // 参数验证失败
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMsg);
        }
        
        try {
            LoginVO loginVO = userService.register(registerDTO);
            return ResponseEntity.ok(loginVO);
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public LoginVO login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("收到登录请求: {}", loginDTO.getAccount());
        return userService.login(loginDTO);
    }
    
    /**
     * 测试接口
     */
    @GetMapping("/test")
    public String test() {
        return "用户服务正常运行";
    }
}