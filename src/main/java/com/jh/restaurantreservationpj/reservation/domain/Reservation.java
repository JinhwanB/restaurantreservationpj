package com.jh.restaurantreservationpj.reservation.domain;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.reservation.dto.CheckReservationDto;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class Reservation {

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
    @Enumerated(EnumType.STRING)
    private ReservationState reservationState; // 점장 예약 승인 여부

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationVisit reservationVisit; // 방문 확인 여부

    // Entity -> CheckResponse
    public CheckReservationDto.Response toCheckResponse() {
        return CheckReservationDto.Response.builder()
                .reservationNumber(reservationNumber)
                .memberId(reservationMember.getUserId())
                .restaurantName(reservationRestaurant.getName())
                .reservationTime(reservationTime)
                .build();
    }
}
