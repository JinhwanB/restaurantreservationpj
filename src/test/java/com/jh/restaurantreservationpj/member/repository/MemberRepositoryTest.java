package com.jh.restaurantreservationpj.member.repository;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void before() {
        MemberRole memberRole = MemberRole.builder()
                .role(Role.ROLE_ADMIN)
                .build();

        List<MemberRole> list = new ArrayList<>();
        list.add(memberRole);

        Member member = Member.builder()
                .userId("test")
                .userPWD("1234")
                .memberRoles(list)
                .build();

        memberRepository.save(member);
    }

    @Test
    @DisplayName("회원 아이디로 회원 찾기")
    void findUser() {
        Member member = memberRepository.findByUserId("test").orElse(null);
        MemberRole memberRole = member.getMemberRoles().get(0);

        assertThat(member.getUserId()).isEqualTo("test");
        assertThat(memberRole.getRole().name()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("회원 아이디 중복 확인")
    void sameId() {
        assertThat(memberRepository.existsByUserId("test")).isEqualTo(true);
    }
}