package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.common.SessionMember;
import com.skala.agentfoundry.dto.LoginRequest;
import com.skala.agentfoundry.dto.MemberResponse;
import com.skala.agentfoundry.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ApiResponse<MemberResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpSession session
    ) {
        MemberResponse member = memberService.authenticate(request);
        session.setAttribute(SessionMember.MEMBER_ID, member.id());
        return ApiResponse.success("로그인되었습니다.", member);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("로그아웃되었습니다.", null);
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(HttpSession session) {
        return ApiResponse.success(
            "현재 회원을 조회했습니다.",
            memberService.getMember(SessionMember.require(session))
        );
    }
}

