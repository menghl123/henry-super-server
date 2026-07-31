package com.henry.user.adapter.controller;

import com.henry.common.entity.PageQuery;
import com.henry.common.result.PageResult;
import com.henry.common.result.Result;
import com.henry.user.application.UserApplicationService;
import com.henry.user.application.dto.CreateUserRequest;
import com.henry.user.application.dto.LoginRequest;
import com.henry.user.application.dto.LoginResponse;
import com.henry.user.application.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(userApplicationService.login(request));
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        return Result.success(userApplicationService.getUserById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<UserDTO>> page(PageQuery query) {
        return Result.success(userApplicationService.pageUsers(query));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateUserRequest request) {
        return Result.success(userApplicationService.createUser(request));
    }
}
