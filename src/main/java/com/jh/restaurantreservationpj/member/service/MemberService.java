package com.jh.restaurantreservationpj.member.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.member.dto.MemberSignUpDto;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 정보 조회 서비스
    @Override
    public UserDetails loadUserByUsername(String username) {
        return memberRepository.findByUserId(username).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    // 회원가입 서비스
    // 비밀번호 암호화
    public String register(MemberSignUpDto.Request request) {
        String userId = request.getUserId().trim();
        String password = request.getPassword().trim();
        List<Role> roles = request.getRoles();

        if (memberRepository.existsByUserId(userId)) { // 아이디가 중복인 경우
            throw new MemberException(MemberErrorCode.ALREADY_EXIST_USERID);
        }

        List<MemberRole> memberRoleList = new ArrayList<>();
        roles.forEach(r -> {
            MemberRole memberRole = MemberRole.builder()
                    .role(r)
                    .build();
            memberRoleList.add(memberRole);
        });

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(password);
        Member newMember = Member.builder()
                .userId(userId)
                .userPWD(encodedPassword)
                .memberRoles(memberRoleList)
                .build();
        memberRepository.save(newMember);

        return userId;
    }

    // 로그인 서비스
    public Member login(MemberSignInDto.Request request) {
        String userId = request.getUserId();
        String password = request.getPassword();

        Member member = memberRepository.findByUserId(userId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (!passwordEncoder.matches(password, member.getPassword())) { // 비밀번호가 다른 경우
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        return member;
    }
}
