package com.jh.restaurantreservationpj.restaurant.service;

import com.jh.restaurantreservationpj.config.CacheKey;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    /**
     * 매장 등록 서비스
     */
    public CreateRestaurantDto.Response createRestaurant(String userId, CreateRestaurantDto.Request request) {
        Member manager = memberRepository.findByUserId(userId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String name = request.getName().trim();
        if (restaurantRepository.existsByName(name)) { // 중복 매장 확인
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
    @CacheEvict(key = "#restaurantName", value = CacheKey.RESTAURANT_KEY)
    @CachePut(key = "#request.name", value = CacheKey.RESTAURANT_KEY)
    public CheckRestaurantDto.Response modifyRestaurant(String userId, String restaurantName, ModifiedRestaurantDto.Request request) {
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        String managerId = restaurant.getManager().getUserId();
        String modifiedName = request.getName().trim();

        if (!userId.equals(managerId)) { // 매장을 관리하는 아이디와 일치하는지 확인
            throw new RestaurantException(RestaurantErrorCode.DIFF_MANAGER);
        }

        if (restaurantRepository.existsByName(modifiedName)) {
            throw new RestaurantException(RestaurantErrorCode.ALREADY_EXIST_NAME);
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

    // 매장 삭제 서비스
    @CacheEvict(key = "#restaurantName", value = CacheKey.RESTAURANT_KEY)
    public String deleteRestaurant(String userId, String restaurantName) {
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        String managerId = restaurant.getManager().getUserId();
        if (!userId.equals(managerId)) { // 매장을 관리하는 아이디와 일치하는지 확인
            throw new RestaurantException(RestaurantErrorCode.DIFF_MANAGER);
        }

        restaurantRepository.delete(restaurant);

        return restaurant.getName();
    }

    // 매장 검색 서비스
    // 검색한 문자로 시작하거나 문자를 포함한 매장을 오름차순으로 정렬하여 가져옴
    // 페이징 처리하여 가져온다.
    @Transactional(readOnly = true)
    public Page<CheckRestaurantDto.Response> searchRestaurantName(String prefix, Pageable pageable) {
        Page<Restaurant> restaurantList = restaurantRepository.findAllByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameAsc(prefix, prefix, pageable);
        List<Restaurant> content = restaurantList.getContent();
        List<CheckRestaurantDto.Response> responseList = content.stream()
                .map(Restaurant::toCheckResponse)
                .toList();

        return new PageImpl<>(responseList, pageable, responseList.size());
    }

    // 매장 상세 조회 서비스
    @Cacheable(key = "#name", value = CacheKey.RESTAURANT_KEY)
    @Transactional(readOnly = true)
    public CheckRestaurantDto.Response checkRestaurant(String name) {
        Restaurant restaurant = restaurantRepository.findByName(name).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        return restaurant.toCheckResponse();
    }

    // 매장 전체 리스트 조회 서비스
    @Transactional(readOnly = true)
    public Page<CheckRestaurantDto.Response> all(Pageable pageable) {
        Page<Restaurant> all = restaurantRepository.findAll(pageable);
        List<Restaurant> content = all.getContent();
        List<CheckRestaurantDto.Response> resultList = content.stream()
                .map(Restaurant::toCheckResponse)
                .toList();

        return new PageImpl<>(resultList, pageable, resultList.size());
    }
}
