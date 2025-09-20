package org.hanseo.boongboong.domain.user.entity; // 패키지 선언

import jakarta.persistence.*; // JPA(Java Persistence API) 어노테이션 사용을 위한 임포트
import lombok.*; // Lombok 라이브러리 임포트 (코드 자동 생성을 위함)
import org.hanseo.boongboong.global.entity.BaseEntity; // 공통 필드(생성/수정일자)를 가진 BaseEntity 상속

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 고유 ID

    @Column(nullable = false, unique = true)
    private String email; // 사용자 이메일

    @Column(nullable = false)
    private String password; // 사용자 비밀번호 (해싱된 값 저장)

    @Column(nullable = false, unique = true)
    private String nickname; // 사용자 닉네임

    @Column(nullable = false)
    private String name; // 사용자 실명

    @Column(nullable = false)
    private int age; // 사용자 나이

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 권한

    private boolean emailVerified; // 이메일 인증 여부

    private int trustScore; // 신뢰 점수
    private int points; // 포인트

    public void setPassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}

