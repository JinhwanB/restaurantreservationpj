package com.jh.restaurantreservationpj.restaurant.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * 매장 등록 컨트롤러
     */
    @PostMapping("/restaurant")
    public ResponseEntity<GlobalResponse<CreateRestaurantDto.Response>> register(@Valid @RequestBody CreateRestaurantDto.Request request){
        // 오픈 시간과 마감 시간은 모두 null(24시간 운영) 이거나 모두 유효한 값이 있어야 함
        // 어느 한 쪽만 null인 경우 exception
        String openTime = request.getOpenTime();
        String closeTime = request.getCloseTime();
        if((openTime == null && closeTime != null) || (openTime != null && closeTime == null)){
            throw new RestaurantException(RestaurantErrorCode.NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME);
        }

        CreateRestaurantDto.Response response = restaurantService.createRestaurant(request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
