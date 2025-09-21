package org.hanseo.boongboong.domain.mypage.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.PostStatus;
import org.hanseo.boongboong.domain.mypage.dto.response.*;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.domain.vehicle.entity.Vehicle;
import org.hanseo.boongboong.domain.vehicle.repository.VehicleRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
/**
 * 마이페이지 서비스 구현.
 * - 사용자 조회 후 프로필/카풀/게시글 정보를 조합
 * - DB 트랜잭션: 기본 readOnly, 쓰기 메서드만 @Transactional
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 기본
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;       // 사용자 조회
    private final VehicleRepository vehicleRepository; // 차량 조회/업서트
    private final PostRepo postRepo;                   // 게시글 조회

    @Override
    public MyPageRes getMyPageInfo(String userEmail) {
        ProfileCardRes profile = profile(userEmail);      // 프로필 카드
        OngoingCarpoolRes ongoing = ongoing(userEmail);   // 진행중 1건(현재 null)
        OngoingCarpoolRes upcoming = upcoming(userEmail); // 가장 가까운 예정 1건
        List<MyPostRes> posts = myPosts(userEmail);       // 내 글 목록
        return new MyPageRes(profile, ongoing, upcoming, posts); // 집합 응답
    }

    @Override
    public ProfileCardRes profile(String userEmail) {
        User user = findUser(userEmail); // 사용자 단건 조회

        var vehicleOpt = vehicleRepository.findByOwnerId(user.getId()); // 내 차량
        ProfileCardRes.VehicleInfoRes vehicleInfo = vehicleOpt
                .map(v -> new ProfileCardRes.VehicleInfoRes(v.getNumber(), v.getImageUrl())) // DTO 매핑
                .orElse(null);

        boolean hasDriverLicense = false; // TODO: 면허 엔티티 연결 시 실제값

        return new ProfileCardRes(
                user.getEmail(),       // 이메일
                user.getNickname(),    // 닉네임
                user.getProfileImg(),  // 프로필 이미지(Data URL)
                user.getRole(),        // 권한
                user.getTrustScore(),  // 신뢰 점수
                hasDriverLicense,      // 면허 여부(추후 연동)
                vehicleInfo            // 차량 정보(null 가능)
        );
    }

    /** 진행중(매칭 시작된) 카풀: 매칭 도메인 도입 전까지는 null */
    @Override
    public OngoingCarpoolRes ongoing(String userEmail) {
        // TODO: 매칭(Trip/Match) 테이블에서 "진행중" 레코드 조회하도록 변경
        return null;
    }

    /** 가장 가까운 예정 1건 */
    @Override
    public OngoingCarpoolRes upcoming(String userEmail) {
        User user = findUser(userEmail); // 사용자
        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalTime now = LocalTime.now();   // 현재 시각
        return postRepo.findNearestUpcomingByAuthor(user.getId(), today, now)
                .stream()             // 리스트로 받아
                .findFirst()          // 첫 1건만 선택(NonUniqueResultException 방지)
                .map(this::toOngoingRes)
                .orElse(null);        // 없으면 null
    }

    @Override
    public List<MyPostRes> myPosts(String userEmail) {
        User user = findUser(userEmail);
        return postRepo.findTop50ByUserIdOrderByCreatedAtDesc(user.getId()) // 최신 50
                .stream()
                .map(this::toMyPostRes) // DTO 매핑
                .toList();
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public ProfileCardRes upsertVehicle(String userEmail, String number, String imageUrl) {
        User user = findUser(userEmail);

        vehicleRepository.findByOwnerId(user.getId())
                .ifPresentOrElse(
                        v -> setVehicleFields(v, number, imageUrl), // 있으면 수정
                        () -> vehicleRepository.save(               // 없으면 신규 저장
                                Vehicle.builder().owner(user).number(number).imageUrl(imageUrl).build()
                        )
                );

        return profile(userEmail); // 변경 후 최신 프로필 반환
    }

    @Transactional // 프록시 필드 직접 세팅(임시) → 엔티티 변경 메서드로 대체 권장
    protected void setVehicleFields(Vehicle v, String number, String imageUrl) {
        try {
            var fNumber = Vehicle.class.getDeclaredField("number"); // 리플렉션으로 number 세팅
            fNumber.setAccessible(true);
            fNumber.set(v, number);

            var fImg = Vehicle.class.getDeclaredField("imageUrl");  // 리플렉션으로 imageUrl 세팅
            fImg.setAccessible(true);
            fImg.set(v, imageUrl);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Vehicle 수정 실패: 엔티티에 변경 메서드를 추가하세요.", e);
        }
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public void deleteVehicle(String userEmail) {
        User user = findUser(userEmail);
        vehicleRepository.findByOwnerId(user.getId())
                .ifPresent(vehicleRepository::delete); // 존재 시 삭제
    }

    /** 예정 전체 리스트 */
    @Override
    public List<OngoingCarpoolRes> upcomingList(String userEmail) {
        User user = findUser(userEmail);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return postRepo.findAllUpcomingByAuthor(user.getId(), today, now)
                .stream()
                .map(this::toOngoingRes)
                .toList();
    }

    // ===== 헬퍼 =====
    private User findUser(String email) {
        return userRepository.findByEmail(email) // 이메일로 단건 조회
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 미존재 처리
    }

    private OngoingCarpoolRes toOngoingRes(Post p) {
        return new OngoingCarpoolRes(
                p.getId(),                 // 게시글 ID
                p.getType(),               // DRIVER/RIDER
                p.getRoute().getFrom(),    // 출발
                p.getRoute().getTo(),      // 도착
                p.getDate(),               // 날짜
                p.getTime()                // 시간
        );
    }

    private MyPostRes toMyPostRes(Post p) {
        PostStatus status = calcStatusByNow(p.getDate(), p.getTime()); // 임시 상태 계산
        return new MyPostRes(
                p.getId(),
                p.getType(),
                status,
                p.getRoute().getFrom(),
                p.getRoute().getTo(),
                p.getDate(),
                p.getTime(),
                p.getSeats() != null ? p.getSeats() : 0 // null 보호
        );
    }

    /** 매칭 전 임시 상태 계산(프론트 표시에만 사용) */
    private PostStatus calcStatusByNow(LocalDate date, LocalTime time) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isAfter(today)) return PostStatus.RECRUITING; // 미래 날짜
        if (date.isBefore(today)) return PostStatus.COMPLETED; // 과거 날짜
        if (time.isAfter(now)) return PostStatus.RECRUITING;   // 오늘, 미래 시간
        if (time.equals(now)) return PostStatus.ONGOING;       // 진행중
        return PostStatus.COMPLETED;                           // 오늘, 과거 시간
    }
}
