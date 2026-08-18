package com.henry.user.adapter.controller;

import com.henry.common.response.StandardPage;
import com.henry.common.response.StandardResponse;
import com.henry.user.application.dto.CreateUserRequest;
import com.henry.user.application.dto.LoginRequest;
import com.henry.user.application.dto.LoginResponse;
import com.henry.user.application.dto.UpdateUserRequest;
import com.henry.user.application.dto.UserDTO;
import com.henry.user.application.dto.UserQuery;
import com.henry.user.application.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public StandardResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return StandardResponse.success(userApplicationService.login(request));
    }

    @GetMapping("/{id}")
    public StandardResponse<UserDTO> getUser(@PathVariable Long id) {
        return StandardResponse.success(userApplicationService.getUserById(id));
    }

    @GetMapping("/page")
    public StandardResponse<StandardPage<UserDTO>> page(UserQuery query) {
        return StandardResponse.success(userApplicationService.pageUsers(query));
    }

    @PostMapping
    public StandardResponse<Long> create(@RequestBody @Valid CreateUserRequest request) {
        return StandardResponse.success(userApplicationService.createUser(request));
    }

    @PutMapping("/{id}")
    public StandardResponse<UserDTO> update(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        return StandardResponse.success(userApplicationService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public StandardResponse<Void> delete(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return StandardResponse.success();
    }
}
