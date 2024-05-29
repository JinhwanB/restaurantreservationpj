package com.jh.restaurantreservationpj.member.repository;

import com.jh.restaurantreservationpj.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId); // 회원 아이디를 통해 회원 찾기

    boolean existsByUserId(String userId); // 회원 아이디 중복 확인
}
