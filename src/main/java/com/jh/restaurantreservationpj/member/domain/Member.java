package com.jh.restaurantreservationpj.member.domain;

import com.jh.restaurantreservationpj.config.BaseTimeEntity;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE restaurant SET del_date = now() WHERE id=?")
@SQLRestriction("del_date IS NULL")
public class Member extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // 아이디

    @Column(nullable = false)
    private String userPWD; // 비밀번호

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
    private List<MemberRole> memberRoles; // 권한

    @Column
    private LocalDateTime delDate; // 삭제 날짜

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        memberRoles.forEach(r -> {
            String role = r.getRole().name();
            authorities.add(() -> role);
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return userPWD;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    // Entity -> LoginResponse
    public MemberSignInDto.Response toLoginResponse() {
        return MemberSignInDto.Response.builder()
                .userId(userId)
                .roles(memberRoles.stream()
                        .map(r -> r.getRole().name())
                        .toList())
                .build();
    }
}
