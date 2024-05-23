package com.jh.restaurantreservationpj.reservation.domain;

import com.jh.restaurantreservationpj.config.BaseTimeEntity;
import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.reservation.dto.CheckForManagerReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE reservation SET del_date = now() WHERE id=?")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationNumber; // 예약 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_number", nullable = false)
    private Member reservationMember; // 예약한 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant reservationRestaurant; // 예약한 식당

    @Column(nullable = false)
    private String reservationTime; // 희망 예약 시간

    @Column(nullable = false)
    private boolean isAccept; // 점장 예약 승인 여부

    @Column
    private String deniedMessage; // 예약 거절 이유

    @Column(nullable = false)
    private boolean isVisit; // 방문 확인 여부

    @Column(nullable = false)
    private boolean isCancel; // 회원이 예약을 취소했는지 여부

    @Column
    private LocalDateTime delDate; // 삭제 날짜

    // Entity -> CheckResponse (점장용)
    public CheckForManagerReservationDto.Response toCheckForManagerResponse() {
        return CheckForManagerReservationDto.Response.builder()
                .reservationNumber(reservationNumber)
                .memberId(reservationMember.getUserId())
                .restaurantName(reservationRestaurant.getName())
                .reservationTime(reservationTime + "시")
                .build();
    }

    // Entity -> CreateResponse
    public CreateReservationDto.Response toCreateResponse() {
        return CreateReservationDto.Response.builder()
                .reservationNumber(reservationNumber)
                .reservationMemberId(reservationMember.getUserId())
                .reservationRestaurantName(reservationRestaurant.getName())
                .reservationTime(reservationTime)
                .build();
    }
}
