package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.restaurant.dto.CheckRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RestaurantServiceTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MockMvc mockMvc;

    CreateRestaurantDto.Request createRequest;
    ModifiedRestaurantDto.Request modifyRequest;
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

    @BeforeEach
    void before() throws Exception {
        String managerJsonData = "{\n" +
                "    \"userId\":\"manager\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"admin\"\n" +
                "    ]\n" +
                "}";

        String anotherManagerJsonData = "{\n" +
                "    \"userId\":\"manager2\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"admin\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJsonData))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anotherManagerJsonData))
                .andExpect(status().isOk());

        createRequest = CreateRestaurantDto.Request.builder()
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        modifyRequest = ModifiedRestaurantDto.Request.builder()
                .name("매장")
                .description("설명2")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();
    }

    @Test
    @DisplayName("매장 등록 서비스")
    void create() {
        CreateRestaurantDto.Response response = restaurantService.createRestaurant("manager", createRequest);

        assertThat(response.getName()).isEqualTo("매장 이름");
    }

    @Test
    @DisplayName("매장 등록 서비스 실패 - 중복된 매장명")
    void failCreate() {
        try {
            restaurantService.createRestaurant("manager", createRequest);
            restaurantService.createRestaurant("manager", createRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.ALREADY_EXIST_NAME.getMessage());
        }
    }

    @Test
    @DisplayName("매장 수정 서비스")
    void modify() {
        restaurantService.createRestaurant("manager", createRequest);
        CheckRestaurantDto.Response response = restaurantService.modifyRestaurant("manager", "매장 이름", modifyRequest);

        assertThat(response.getDescription()).isEqualTo("설명2");
    }

    @Test
    @DisplayName("매장 수정 서비스 실패 - 없는 매장")
    void failModify1() {
        restaurantService.createRestaurant("manager", createRequest);
        try {
            restaurantService.modifyRestaurant("manager", "매장", modifyRequest);
        } catch (RestaurantException e) {
            assertThat(e.getRestaurantErrorCode().getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("매장 수정 서비스 실패 - 매장 관리자와 아이디 다름")
    void failModify2() {
        restaurantService.createRestaurant("manager", createRequest);
        try {
            restaurantService.modifyRestaurant("manager2", "매장 이름", modifyRequest);
        } catch (RestaurantException e) {
            assertThat(e.getRestaurantErrorCode().getMessage()).isEqualTo(RestaurantErrorCode.DIFF_MANAGER.getMessage());
        }
    }

    @Test
    @DisplayName("매장 수정 서비스 실패 - 이미 존재하는 매장")
    void failModify3() {
        restaurantService.createRestaurant("manager", createRequest);

        ModifiedRestaurantDto.Request badRequest = modifyRequest.toBuilder()
                .name("매장 이름")
                .build();

        try {
            restaurantService.modifyRestaurant("manager", "매장 이름", badRequest);
        } catch (RestaurantException e) {
            assertThat(e.getRestaurantErrorCode().getMessage()).isEqualTo(RestaurantErrorCode.ALREADY_EXIST_NAME.getMessage());
        }
    }

    @Test
    @DisplayName("매장 삭제 서비스")
    void delete() {
        restaurantService.createRestaurant("manager", createRequest);

        restaurantService.deleteRestaurant("manager", "매장 이름");

        assertThat(restaurantRepository.existsByName("매장 이름")).isEqualTo(false);
    }

    @Test
    @DisplayName("매장 삭제 서비스 실패 - 없는 매장")
    void failDelete1() {
        restaurantService.createRestaurant("manager", createRequest);

        try {
            restaurantService.deleteRestaurant("manager", "매장");
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("매장 삭제 서비스 실패 - 매장 관리자와 아이디 다름")
    void failDelete2() {
        restaurantService.createRestaurant("manager", createRequest);

        try {
            restaurantService.deleteRestaurant("manager2", "매장 이름");
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.DIFF_MANAGER.getMessage());
        }
    }

    @Test
    @DisplayName("매장 검색 서비스")
    void search() {
        restaurantService.createRestaurant("manager", createRequest);

        CreateRestaurantDto.Request secondCreateRequest = createRequest.toBuilder()
                .name("매가")
                .build();
        restaurantService.createRestaurant("manager", secondCreateRequest);

        Page<CheckRestaurantDto.Response> searched = restaurantService.searchRestaurantName("매", pageable);
        List<CheckRestaurantDto.Response> content = searched.getContent();

        assertThat(content).hasSize(2);
        assertThat(content.get(0).getName()).isEqualTo("매가");
        assertThat(content.get(1).getName()).isEqualTo("매장 이름");
    }

    @Test
    @DisplayName("매장 상세 조회 서비스")
    void check() {
        restaurantService.createRestaurant("manager", createRequest);

        CheckRestaurantDto.Response response = restaurantService.checkRestaurant("매장 이름");

        assertThat(response.getName()).isEqualTo("매장 이름");
    }

    @Test
    @DisplayName("매장 상세 조회 서비스 실패 - 없는 매장")
    void failCheck() {
        try {
            restaurantService.checkRestaurant("매장 이름");
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }
}