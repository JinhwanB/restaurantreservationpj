package com.jh.restaurantreservationpj.restaurant.domain;

import com.jh.restaurantreservationpj.config.BaseTimeEntity;
import com.jh.restaurantreservationpj.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE restaurant SET del_date = now() WHERE id=?")
@SQLRestriction("del_date IS NULL")
public class Restaurant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member manager; // 관리자(점장)

    @Column(nullable = false, unique = true)
    private String name; // 매장명

    @Column(nullable = false)
    private String totalAddress; // 주소

    @Column(length = 5000)
    private String description; // 매장 설명

    @Column
    private String openTime; // 오픈 시간(null인 경우 24시간 운영)

    @Column
    private String closeTime; // 마감 시간

    @Column
    private LocalDateTime delDate; // 삭제 날짜
}
