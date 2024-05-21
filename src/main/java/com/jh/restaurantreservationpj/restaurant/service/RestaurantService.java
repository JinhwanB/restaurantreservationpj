package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    /**
     * 매장 등록 서비스
     */
    public CreateRestaurantDto.Response createRestaurant(CreateRestaurantDto.Request request){
        String userId = request.getUserId();
        Member manager = memberRepository.findByUserId(userId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String name = request.getName();
        if(restaurantRepository.existsByName(name)){
            throw new RestaurantException(RestaurantErrorCode.ALREADY_EXIST_NAME);
        }
        
        Restaurant restaurant = Restaurant.builder()
                .manager(manager)
                .name(request.getName())
                .totalAddress(request.getTotalAddress())
                .description(request.getDescription())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();
        Restaurant save = restaurantRepository.save(restaurant);
        return save.toCreateResponse();
    }
}
