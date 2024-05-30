package com.jh.restaurantreservationpj.restaurant.controller;

import com.jh.restaurantreservationpj.auth.TokenProvider;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.restaurant.dto.CheckRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.service.RestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Validated
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final TokenProvider tokenProvider;

    /**
     * 매장 등록 컨트롤러
     */
    @PostMapping("/restaurant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<CreateRestaurantDto.Response>> register(@Valid @RequestBody CreateRestaurantDto.Request request, HttpServletRequest servletRequest) {
        String userId = tokenProvider.getUserId(servletRequest);

        // 오픈시간과 마감시간 유효성 검증
        String openTime = request.getOpenTime() != null ? request.getOpenTime().trim() : null;
        String closeTime = request.getCloseTime() != null ? request.getCloseTime().trim() : null;
        validOfOpenTimeAndCloseTime(openTime, closeTime);

        CreateRestaurantDto.Response response = restaurantService.createRestaurant(userId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 매장 수정 컨트롤러
    @PutMapping("/restaurant/{restaurantName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<CheckRestaurantDto.Response>> modify(@PathVariable @NotBlank(message = "매장 이름은 필수로 입력하여야 합니다.") String restaurantName, @Valid @RequestBody ModifiedRestaurantDto.Request request, HttpServletRequest servletRequest) {
        String userId = tokenProvider.getUserId(servletRequest);

        String openTime = request.getOpenTime() != null ? request.getOpenTime().trim() : null;
        String closeTime = request.getCloseTime() != null ? request.getCloseTime().trim() : null;
        validOfOpenTimeAndCloseTime(openTime, closeTime); // 오픈시간과 마감시간 유효성 검사

        CheckRestaurantDto.Response response = restaurantService.modifyRestaurant(userId, restaurantName, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 매장 삭제 컨트롤러
    @DeleteMapping("/restaurant/{restaurantName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<String>> delete(@NotBlank(message = "매장 이름을 입력해주세요.") @PathVariable String restaurantName, HttpServletRequest servletRequest) {
        String userId = tokenProvider.getUserId(servletRequest);
        String deletedRestaurant = restaurantService.deleteRestaurant(userId, restaurantName);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(deletedRestaurant));
    }

    // 매장 검색 컨트롤러
    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<Page<CheckRestaurantDto.Response>>> search(@RequestParam @NotBlank(message = "검색어를 입력해주세요.") String word, @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CheckRestaurantDto.Response> searched = restaurantService.searchRestaurantName(word, pageable);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(searched));
    }

    // 매장 상세 조회 컨트롤러
    @GetMapping("/restaurant/{restaurantName}")
    public ResponseEntity<GlobalResponse<CheckRestaurantDto.Response>> check(@PathVariable @NotBlank(message = "매장 이름을 입력해주세요.") String restaurantName) {
        CheckRestaurantDto.Response response = restaurantService.checkRestaurant(restaurantName);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 오픈시간과 마감시간 유효성 검증
    private static void validOfOpenTimeAndCloseTime(String openTime, String closeTime) {
        // 오픈 시간과 마감 시간은 모두 null(24시간 운영) 이거나 모두 유효한 값이 있어야 함
        // 어느 한 쪽만 null인 경우 exception
        if ((openTime == null && closeTime != null) || (openTime != null && closeTime == null)) {
            throw new RestaurantException(RestaurantErrorCode.NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME);
        }
    }
}
