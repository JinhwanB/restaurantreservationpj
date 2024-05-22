package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.dto.CheckRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.DeleteRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
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
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantService restaurantService;

    CreateRestaurantDto.Request createRequest;
    ModifiedRestaurantDto.Request modifyRequest;
    DeleteRestaurantDto.Request deleteRequest;

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

        createRequest = CreateRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        modifyRequest = ModifiedRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명2")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        deleteRequest = DeleteRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .build();
    }

    @Test
    @DisplayName("매장 등록 서비스")
    void create() {
        CreateRestaurantDto.Response response = restaurantService.createRestaurant(createRequest);

        assertThat(response.getName()).isEqualTo("매장 이름");
    }

    @Test
    @DisplayName("매장 등록 서비스 실패 - 중복된 매장명")
    void failCreate() {
        try {
            restaurantService.createRestaurant(createRequest);
            restaurantService.createRestaurant(createRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.ALREADY_EXIST_NAME.getMessage());
        }
    }

    @Test
    @DisplayName("매장 수정 서비스")
    void modify() {
        restaurantService.createRestaurant(createRequest);
        CheckRestaurantDto.Response response = restaurantService.modifyRestaurant("매장 이름", modifyRequest);

        assertThat(response.getDescription()).isEqualTo("설명2");
    }

    @Test
    @DisplayName("매장 수정 서비스 실패 - 없는 매장")
    void failModify1() {
        restaurantService.createRestaurant(createRequest);
        try {
            restaurantService.modifyRestaurant("매장", modifyRequest);
        } catch (RestaurantException e) {
            assertThat(e.getRestaurantErrorCode().getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("매장 수정 서비스 실패 - 매장 관리자와 아이디 다름")
    void failModify2() {
        restaurantService.createRestaurant(createRequest);
        try {
            restaurantService.modifyRestaurant("매장 이름", modifyRequest.toBuilder().userId("te").build());
        } catch (RestaurantException e) {
            assertThat(e.getRestaurantErrorCode().getMessage()).isEqualTo(RestaurantErrorCode.DIFF_MANAGER.getMessage());
        }
    }

    @Test
    @DisplayName("매장 삭제 서비스")
    void delete() {
        restaurantService.createRestaurant(createRequest);

        restaurantService.deleteRestaurant(deleteRequest);

        assertThat(restaurantRepository.existsByName("매장 이름")).isEqualTo(false);
    }

    @Test
    @DisplayName("매장 삭제 서비스 실패 - 없는 매장")
    void failDelete1() {
        restaurantService.createRestaurant(createRequest);

        DeleteRestaurantDto.Request badRequest = deleteRequest.toBuilder()
                .name("매장")
                .build();
        try {
            restaurantService.deleteRestaurant(badRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("매장 삭제 서비스 실패 - 매장 관리자와 아이디 다름")
    void failDelete2() {
        restaurantService.createRestaurant(createRequest);

        DeleteRestaurantDto.Request badRequest = deleteRequest.toBuilder()
                .userId("ttt")
                .build();
        try {
            restaurantService.deleteRestaurant(badRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.DIFF_MANAGER.getMessage());
        }
    }
}