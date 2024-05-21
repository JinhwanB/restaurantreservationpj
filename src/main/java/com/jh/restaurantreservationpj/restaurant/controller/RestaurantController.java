package com.jh.restaurantreservationpj.restaurant.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.restaurant.dto.CheckRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Validated
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * 매장 등록 컨트롤러
     */
    @PostMapping("/restaurant")
    public ResponseEntity<GlobalResponse<CreateRestaurantDto.Response>> register(@Valid @RequestBody CreateRestaurantDto.Request request){
        // 오픈시간과 마감시간 유효성 검증
        String openTime = request.getOpenTime().trim();
        String closeTime = request.getCloseTime().trim();
        validOfOpenTimeAndCloseTime(openTime, closeTime);

        CreateRestaurantDto.Response response = restaurantService.createRestaurant(request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 매장 수정 컨트롤러
    @PutMapping("/restaurant/{restaurantName}")
    public ResponseEntity<GlobalResponse<CheckRestaurantDto.Response>> modify(@PathVariable @NotBlank String restaurantName, @Valid @RequestBody ModifiedRestaurantDto.Request request){
        String openTime = request.getOpenTime().trim();
        String closeTime = request.getCloseTime().trim();
        validOfOpenTimeAndCloseTime(openTime, closeTime); // 오픈시간과 마감시간 유효성 검사

        CheckRestaurantDto.Response response = restaurantService.modifyRestaurant(restaurantName, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 오픈시간과 마감시간 유효성 검증
    private static void validOfOpenTimeAndCloseTime(String openTime, String closeTime) {
        // 오픈 시간과 마감 시간은 모두 null(24시간 운영) 이거나 모두 유효한 값이 있어야 함
        // 어느 한 쪽만 null인 경우 exception
        if((openTime == null && closeTime != null) || (openTime != null && closeTime == null)){
            throw new RestaurantException(RestaurantErrorCode.NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME);
        }
    }
}
