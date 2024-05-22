package com.jh.restaurantreservationpj.restaurant.repository;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

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

        Member save = memberRepository.save(member);

        Restaurant restaurant = Restaurant.builder()
                .name("매장 이름")
                .closeTime("22")
                .openTime("08")
                .description("설명")
                .totalAddress("주소")
                .manager(save)
                .build();
        restaurantRepository.save(restaurant);

        Restaurant restaurant2 = Restaurant.builder()
                .name("매가")
                .closeTime("21")
                .openTime("09")
                .description("설명2")
                .totalAddress("주소2")
                .manager(save)
                .build();
        restaurantRepository.save(restaurant2);
    }

    @Test
    @DisplayName("매장명으로 매장 존재 여부 확인")
    void existByName() {
        assertThat(restaurantRepository.existsByName("매장 이름")).isEqualTo(true);
    }

    @Test
    @DisplayName("매장명으로 매장 찾기")
    void findByName() {
        Restaurant restaurant = restaurantRepository.findByName("매장 이름").orElse(null);

        assertThat(restaurant).isNotNull();
    }

    @Test
    @DisplayName("매장 검색 쿼리 메소드")
    void search() {
        Page<Restaurant> restaurantList = restaurantRepository.findAllByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameAsc("매", "매", pageable);
        List<Restaurant> content = restaurantList.getContent();

        assertThat(content).hasSize(2);
        assertThat(content.get(0).getName()).isEqualTo("매가");
        assertThat(content.get(1).getName()).isEqualTo("매장 이름");
    }
}