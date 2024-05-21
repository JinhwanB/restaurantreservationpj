package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.restaurant.dto.CheckRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
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
        String userId = request.getUserId().trim();
        Member manager = memberRepository.findByUserId(userId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String name = request.getName().trim();
        if(restaurantRepository.existsByName(name)){ // 중복 매장 확인
            throw new RestaurantException(RestaurantErrorCode.ALREADY_EXIST_NAME);
        }
        
        Restaurant restaurant = Restaurant.builder()
                .manager(manager)
                .name(name)
                .totalAddress(request.getTotalAddress().trim())
                .description(request.getDescription().trim())
                .openTime(request.getOpenTime() != null ? request.getOpenTime().trim() : null)
                .closeTime(request.getCloseTime() != null ? request.getCloseTime().trim() : null)
                .build();
        Restaurant save = restaurantRepository.save(restaurant);
        return save.toCreateResponse();
    }

    // 매장 수정 서비스
    public CheckRestaurantDto.Response modifyRestaurant(String restaurantName, ModifiedRestaurantDto.Request request){
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        String userId = request.getUserId().trim();
        String manager = restaurant.getManager().getUserId();
        if(!userId.equals(manager)){ // 매장을 관리하는 아이디와 일치하는지 확인
            throw new RestaurantException(RestaurantErrorCode.DIFF_MANAGER);
        }

        // 수정 내용으로 변경
        Restaurant modified = restaurant.toBuilder()
                .name(request.getName().trim())
                .totalAddress(request.getTotalAddress().trim())
                .description(request.getDescription().trim())
                .openTime(request.getOpenTime() != null ? request.getOpenTime().trim() : null)
                .closeTime(request.getCloseTime() != null ? request.getCloseTime().trim() : null)
                .build();
        Restaurant save = restaurantRepository.save(modified);
        return save.toCheckResponse();
    }
}
