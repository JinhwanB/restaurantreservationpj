package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
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
class RestaurantServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantService restaurantService;

    CreateRestaurantDto.Request request;

    @BeforeEach
    void before(){
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

        request = CreateRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();
    }

    @Test
    @DisplayName("매장 등록 서비스")
    void create(){
        CreateRestaurantDto.Response response = restaurantService.createRestaurant(request);

        assertThat(response.getName()).isEqualTo("매장 이름");
    }

    @Test
    @DisplayName("매장 등록 서비스 실패 - 중복된 매장명")
    void failCreate(){
        try{
            restaurantService.createRestaurant(request);
            restaurantService.createRestaurant(request);
        }catch (RestaurantException e){
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.ALREADY_EXIST_NAME.getMessage());
        }
    }
}