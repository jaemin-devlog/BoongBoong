package org.hanseo.boongboong.domain.user.service;

import org.hanseo.boongboong.domain.user.entity.AvatarColor;
import org.hanseo.boongboong.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * 가입/닉네임 변경 시 1회만 호출되는 아바타 생성기.
 * - 닉네임의 첫 글자에서 이니셜을 정한다. (한글 → 초성 → 로마자 1글자: 예) "정..." → 'J')
 * - 닉네임을 seed로 안정적 색상을 선택한다.
 * - 배경색 대비를 계산해 텍스트를 흰/검 중 자동 선택한다(WCAG 근사).
 * - SVG를 Data URL로 만들어 User에 고정 저장한다(파일/엔드포인트 불필요).
 */
// 역할: 닉네임(우선) 기반으로 아바타 이니셜/배경색/SVG Data URL 생성해 User에 세팅하는 컴포넌트
// 특징: 파일 저장/별도 엔드포인트 없이 Data URL로 즉시 사용, 닉네임 seed로 색상 안정적 선택
@Component
public class AvatarGenerator {

    private static final Map<AvatarColor, String> COLOR_HEX = new EnumMap<>(AvatarColor.class); // 색상→HEX 매핑
    static {
        // 비브런트 팔레트
        COLOR_HEX.put(AvatarColor.RED,        "#EF4444");
        COLOR_HEX.put(AvatarColor.ORANGE,     "#F97316");
        COLOR_HEX.put(AvatarColor.YELLOW,     "#F59E0B");
        COLOR_HEX.put(AvatarColor.GREEN,      "#10B981");
        COLOR_HEX.put(AvatarColor.TEAL,       "#14B8A6");
        COLOR_HEX.put(AvatarColor.BLUE,       "#3B82F6");
        COLOR_HEX.put(AvatarColor.INDIGO,     "#6366F1");
        COLOR_HEX.put(AvatarColor.PURPLE,     "#8B5CF6");
        COLOR_HEX.put(AvatarColor.PINK,       "#EC4899");
        COLOR_HEX.put(AvatarColor.BROWN,      "#92400E");
        COLOR_HEX.put(AvatarColor.GRAY,       "#6B7280");

        // 파스텔 팔레트
        COLOR_HEX.put(AvatarColor.MINT,       "#A7F3D0");
        COLOR_HEX.put(AvatarColor.LIME,       "#D9F99D");
        COLOR_HEX.put(AvatarColor.SKY,        "#BAE6FD");
        COLOR_HEX.put(AvatarColor.CORAL,      "#FCA5A5");
        COLOR_HEX.put(AvatarColor.PEACH,      "#FCD5B5");
        COLOR_HEX.put(AvatarColor.LAVENDER,   "#E9D5FF");
        COLOR_HEX.put(AvatarColor.LILAC,      "#EBD4FF");
        COLOR_HEX.put(AvatarColor.PERIWINKLE, "#C7D2FE");
        COLOR_HEX.put(AvatarColor.APRICOT,    "#FED7AA");
        COLOR_HEX.put(AvatarColor.SALMON,     "#FBC4AB");
        COLOR_HEX.put(AvatarColor.SAGE,       "#D1FAE5");
        COLOR_HEX.put(AvatarColor.SAND,       "#F5E6CC");
        COLOR_HEX.put(AvatarColor.BEIGE,      "#F3E8D2");
        COLOR_HEX.put(AvatarColor.ICE,        "#E0F2FE");
        COLOR_HEX.put(AvatarColor.BLUSH,      "#F8D7DA");
    }

    /** 가입 시 1회 호출: letter/color/profileImg(Data URL) 확정 후 User에 고정 저장 */
    public void applyOnSignup(User user) {
        Objects.requireNonNull(user, "user");                    // null 방지
        String letter = deriveLetter(user.getNickname(), user.getEmail()); // 이니셜 계산
        AvatarColor color = pickStableColor(seedOf(user));       // 안정적 색상 선택
        String dataUrl = buildSvgDataUrl(letter, color);         // SVG Data URL 생성
        user.assignAvatar(letter, color, dataUrl);               // User에 세팅
    }

    /** 닉네임 변경 시 재생성: 닉네임 변경 이후에 호출해야 함 */
    public void applyOnNicknameChange(User user) {
        Objects.requireNonNull(user, "user");
        String letter = deriveLetter(user.getNickname(), user.getEmail()); // 변경된 닉네임 기준
        AvatarColor color = pickStableColor(seedOf(user));                 // 변경 닉네임 seed
        String dataUrl = buildSvgDataUrl(letter, color);
        user.assignAvatar(letter, color, dataUrl);
    }

    /** 닉네임(우선) → 닉네임이 비면 이메일로 대체. 한글은 초성→로마자 맵으로 1글자 */
    public String deriveLetter(String nickname, String email) {
        String src = isBlank(nickname) ? email : nickname; // 닉 우선, 없으면 이메일
        if (isBlank(src)) return "A";                      // 둘 다 없으면 기본값
        char first = src.charAt(0);                        // 첫 글자

        // 1) 한글 음절(U+AC00~U+D7A3) 처리 → 초성 → 로마자 첫 글자
        if (first >= 0xAC00 && first <= 0xD7A3) {
            char initial = romanInitialFromHangul(first);  // 예: '정' → 'J'
            return String.valueOf(initial);
        }

        // 2) 영문/숫자 등: 영문이면 대문자, 그 외는 A
        if (Character.isLetter(first)) return String.valueOf(Character.toUpperCase(first));
        return "A";
    }

    /** 한글 초성 → 로마자 첫 글자(간이표). 예: ㅈ,ㅉ → J / ㅊ → C / ㅅ,ㅆ → S / ㅇ → Y */
    private char romanInitialFromHangul(char syllable) {
        // 한글 분해: (code - 0xAC00) = choseongIndex*588 + jungseongIndex*28 + jongseongIndex
        int base = syllable - 0xAC00;
        int choseong = base / 588; // 0~18

        // 초성 19개 테이블: ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ
        final char[] TABLE = {
                'K','G','N','D','D','R','M','B','B','S','S','Y','J','J','C','K','T','P','H'
        };
        return TABLE[Math.max(0, Math.min(choseong, TABLE.length - 1))]; // 범위 보호
    }

    /** 안정적 색상 선택: 닉네임을 seed로 사용(없으면 이메일→ID 순) */
    private AvatarColor pickStableColor(String seed) {
        AvatarColor[] values = AvatarColor.values();  // 열거값 배열
        int idx = Math.abs(seed.hashCode()) % values.length; // 해시 기반 인덱스
        return values[idx];
    }

    private String seedOf(User u) {
        if (!isBlank(u.getNickname())) return u.getNickname(); // 닉네임 우선
        if (!isBlank(u.getEmail())) return u.getEmail();       // 그다음 이메일
        return String.valueOf(u.getId() != null ? u.getId() : 0L); // 최후 ID
    }

    /** SVG를 Data URL로 반환 → 정적 파일/엔드포인트 불필요, 한번 저장 후 고정 (대비 자동 전환 포함) */
    public String buildSvgDataUrl(String letter, AvatarColor color) {
        String bgHex = COLOR_HEX.getOrDefault(color, "#6B7280");  // 배경색 HEX
        String fgHex = pickReadableTextHex(bgHex);                // 배경 대비 텍스트 색
        String safe = isBlank(letter) ? "A" : letter.substring(0,1).toUpperCase(); // 1글자 보정

        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">
              <defs>
                <style>
                  .bg { fill: %s; }
                  .txt { fill: %s; font-size: 128px; font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Arial,sans-serif; font-weight: 700; }
                </style>
              </defs>
              <circle class="bg" cx="128" cy="128" r="128"/>
              <text class="txt" x="50%%" y="50%%" text-anchor="middle" dominant-baseline="central">%s</text>
            </svg>
            """.formatted(bgHex, fgHex, safe); // SVG 문자열 조립

        // Data URL 인코딩(공백 → %20)
        String enc = URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
        return "data:image/svg+xml;utf8," + enc; // 최종 Data URL
    }

    /** 배경 HEX에 대해 가독성 좋은 텍스트 색(흰/검)을 선택(WCAG 상대 휘도 기준 근사) */
    private String pickReadableTextHex(String bgHex) {
        int[] rgb = hexToRgb(bgHex);                 // HEX → RGB
        double l = relativeLuminance(rgb[0], rgb[1], rgb[2]); // 상대 휘도(0~1)
        return (l > 0.179) ? "#111827" : "#FFFFFF";  // 밝으면 검정, 어두우면 흰색
    }

    private static int[] hexToRgb(String hex) {
        String s = hex.startsWith("#") ? hex.substring(1) : hex; // '#' 제거
        int r = Integer.parseInt(s.substring(0,2), 16);
        int g = Integer.parseInt(s.substring(2,4), 16);
        int b = Integer.parseInt(s.substring(4,6), 16);
        return new int[]{r,g,b};
    }

    // WCAG: 상대 휘도 계산
    private static double relativeLuminance(int r, int g, int b) {
        double rs = r/255.0, gs = g/255.0, bs = b/255.0; // 정규화
        double R = (rs <= 0.03928) ? rs/12.92 : Math.pow((rs+0.055)/1.055, 2.4);
        double G = (gs <= 0.03928) ? gs/12.92 : Math.pow((gs+0.055)/1.055, 2.4);
        double B = (bs <= 0.03928) ? bs/12.92 : Math.pow((bs+0.055)/1.055, 2.4);
        return 0.2126*R + 0.7152*G + 0.0722*B;        // 상대 휘도
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); } // 공백 체크
}
