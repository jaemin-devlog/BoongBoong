package org.hanseo.boongboong.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.global.entity.BaseEntity;

/**
 * 사용자 엔티티.
 * - 이메일/닉네임 유니크, 아바타(문자/색/이미지) 기본값 자동 세팅
 */
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
    private String password; // 해시된 비밀번호

    @Column(nullable = false, unique = true)
    private String nickname; // 닉네임(서비스 노출명)

    @Column(nullable = false)
    private String name; // 실명(내부용)

    @Column(nullable = false)
    private int age; // 나이

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 권한

    private boolean emailVerified; // 이메일 인증 여부
    private int trustScore;        // 신뢰 점수
    private boolean hasDriverLicense; // 운전면허 여부

    @Column(name = "profile_img", columnDefinition = "TEXT")
    private String profileImg; // 프로필 이미지 Data URL (SVG)

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_color", length = 20)
    private AvatarColor avatarColor; // 기본 아바타 색

    @Column(name = "avatar_letter", length = 1)
    private String avatarLetter; // A~Z 한 글자(닉네임 기반)

    /** 신규 유저 저장 시 누락 값 기본 세팅(이미 값이 있으면 건드리지 않음) */
    @PrePersist
    public void initAvatar() {
        if (avatarLetter == null || avatarLetter.isBlank()) {
            String src = (nickname != null && !nickname.isBlank()) ? nickname : email;
            char c = Character.toUpperCase(src.charAt(0));
            this.avatarLetter = (c >= 'A' && c <= 'Z') ? String.valueOf(c) : "A";
        }
        if (avatarColor == null) {
            AvatarColor[] cs = AvatarColor.values();
            String seed = (nickname != null && !nickname.isBlank()) ? nickname : email; // 닉네임 기반 색상 고정
            int idx = Math.abs(seed.hashCode()) % cs.length;
            this.avatarColor = cs[idx];
        }
        if (trustScore == 0) this.trustScore = 50;
        if (!hasDriverLicense) this.hasDriverLicense = false;
    }

    /** 비밀번호 변경(인코딩된 값으로만 세팅) */
    public void setPassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 닉네임 변경(외부 서비스에서 중복 검증 후 호출) */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /** 아바타(문자/색상/이미지) 확정 세팅 */
    public void assignAvatar(String letter, AvatarColor color, String profileImgDataUrlOrUrl) {
        this.avatarLetter = letter;
        this.avatarColor = color;
        this.profileImg = profileImgDataUrlOrUrl;
    }
}
