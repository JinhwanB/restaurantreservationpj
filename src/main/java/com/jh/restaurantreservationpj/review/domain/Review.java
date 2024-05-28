package com.jh.restaurantreservationpj.review.domain;

import com.jh.restaurantreservationpj.config.BaseTimeEntity;
import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
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
@SQLDelete(sql = "UPDATE review SET del_date = now() WHERE id=?")
@SQLRestriction(value = "del_date IS NULL")
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_key", nullable = false)
    private Restaurant restaurant; // 매장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_key", nullable = false)
    private Member member; // 작성자

    @Column(nullable = false)
    private String title; // 제목

    @Column(length = 5000, nullable = false)
    private String content; // 내용

    @Column
    private LocalDateTime delDate; // 삭제 날짜

    // Entity -> CreateResponse
    public CreateReviewDto.Response toCreateResponse() {
        return CreateReviewDto.Response.builder()
                .id(id)
                .title(title)
                .content(content)
                .build();
    }

    // Entity -> ModifyResponse
    public ModifyReviewDto.Response toModifyResponse() {
        return ModifyReviewDto.Response.builder()
                .id(id)
                .title(title)
                .content(content)
                .build();
    }
}
