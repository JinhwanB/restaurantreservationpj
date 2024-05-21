package com.jh.restaurantreservationpj.member.domain;

import com.jh.restaurantreservationpj.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE restaurant SET del_date = now() WHERE id=?")
@SQLRestriction("del_date IS NULL")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // 아이디

    @Column(nullable = false)
    private String userPWD; // 비밀번호

    @OneToMany(cascade = CascadeType.ALL)
    private List<MemberRole> memberRoles; // 권한

    @Column
    private LocalDateTime delDate; // 삭제 날짜
}
