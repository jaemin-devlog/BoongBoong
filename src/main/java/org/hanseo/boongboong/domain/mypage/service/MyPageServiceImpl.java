package org.hanseo.boongboong.domain.mypage.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.PostStatus;
import org.hanseo.boongboong.domain.mypage.dto.response.*;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.hanseo.boongboong.domain.vehicle.entity.Vehicle;
import org.hanseo.boongboong.domain.vehicle.repository.VehicleRepository;
import org.hanseo.boongboong.domain.license.entity.DriverLicense;
import org.hanseo.boongboong.domain.license.repository.DriverLicenseRepository;
import org.hanseo.boongboong.domain.mypage.dto.request.LicenseReq;
import org.hanseo.boongboong.domain.mypage.dto.request.FullProfileUpdateReq;
import org.hanseo.boongboong.domain.mypage.dto.response.LicenseRes;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// match
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.match.entity.MatchMember;
import org.hanseo.boongboong.domain.match.repository.MatchMemberRepo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PostRepo postRepo;
    private final DriverLicenseRepository driverLicenseRepository;
    private final UserService userService;
    private final MatchMemberRepo matchMemberRepo;

    @Override
    public MyPageRes getMyPageInfo(String userEmail) {
        ProfileCardRes profile = profile(userEmail);
        OngoingCarpoolRes ongoing = ongoing(userEmail);
        OngoingCarpoolRes upcoming = upcoming(userEmail);
        List<MyPostRes> posts = myPosts(userEmail);
        return new MyPageRes(profile, ongoing, upcoming, posts);
    }

    @Override
    public ProfileCardRes profile(String userEmail) {
        User user = findUser(userEmail);

        var vehicleOpt = vehicleRepository.findByOwnerId(user.getId());
        ProfileCardRes.VehicleInfoRes vehicleInfo = vehicleOpt
                .map(v -> new ProfileCardRes.VehicleInfoRes(v.getNumber(), v.getImageUrl(), v.getSeats(), v.getColor()))
                .orElse(null);

        boolean hasDriverLicense = driverLicenseRepository.findByOwnerId(user.getId()).isPresent();
        return new ProfileCardRes(
                user.getEmail(),
                user.getNickname(),
                user.getProfileImg(),
                user.getRole(),
                user.getTrustScore(),
                hasDriverLicense,
                user.getIntro(),
                user.getOpenChatUrl(),
                vehicleInfo
        );
    }

    @Override
    public OngoingCarpoolRes ongoing(String userEmail) {
        // 현재 진행중인 매칭(오늘, 현재시간 이상 포함) 중 가장 가까운 건 반환
        User user = findUser(userEmail);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return matchMemberRepo.findAllUpcomingByUserEmail(user.getEmail(), today, now)
                .stream()
                .findFirst()
                .map(this::toOngoingResFromMatch)
                .orElse(null);
    }

    @Override
    public OngoingCarpoolRes upcoming(String userEmail) {
        // 가장 가까운 ‘성사된(매칭)’ 카풀 1건 반환
        User user = findUser(userEmail);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return matchMemberRepo.findAllUpcomingByUserEmail(user.getEmail(), today, now)
                .stream()
                .findFirst()
                .map(this::toOngoingResFromMatch)
                .orElse(null);
    }

    @Override
    public List<MyPostRes> myPosts(String userEmail) {
        User user = findUser(userEmail);
        return postRepo.findTop50ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toMyPostRes)
                .toList();
    }

    @Override
    @Transactional
    public ProfileCardRes upsertVehicle(String userEmail, String number, String imageUrl, Integer seats, String color) {
        User user = findUser(userEmail);
        vehicleRepository.findByOwnerId(user.getId())
                .ifPresentOrElse(
                        v -> v.update(number, imageUrl, seats, color),
                        () -> vehicleRepository.save(Vehicle.builder()
                                .owner(user)
                                .number(number)
                                .imageUrl(imageUrl)
                                .seats(seats)
                                .color(color)
                                .build())
                );
        return profile(userEmail);
    }

    @Override
    @Transactional
    public void deleteVehicle(String userEmail) {
        User user = findUser(userEmail);
        vehicleRepository.findByOwnerId(user.getId()).ifPresent(vehicleRepository::delete);
    }

    @Override
    public List<OngoingCarpoolRes> upcomingList(String userEmail) {
        User user = findUser(userEmail);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return matchMemberRepo.findAllUpcomingByUserEmail(user.getEmail(), today, now)
                .stream()
                .map(this::toOngoingResFromMatch)
                .toList();
    }

    @Override
    public List<CompletedCarpoolRes> completedList(String userEmail) {
        User user = findUser(userEmail);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return matchMemberRepo.findAllCompletedByUserEmail(user.getEmail(), today, now)
                .stream()
                .map(this::toCompletedCarpoolResFromMatch)
                .toList();
    }

    @Override
    @Transactional
    public void updateProfile(String userEmail, String name, Integer age, String profileImg, String intro, String openChatUrl) {
        User user = findUser(userEmail);
        user.updateProfileBasics(name, age, profileImg, intro, openChatUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void saveFullProfile(String userEmail, FullProfileUpdateReq req) {
        // 1) 닉네임 변경(검증/중복 체크 포함)
        if (req.nickname() != null && !req.nickname().isBlank()) {
            userService.updateNicknameByEmail(userEmail, req.nickname());
        }
        // 2) 나머지 기본 정보 갱신
        updateProfile(userEmail, req.name(), req.age(), req.profileImg(), req.intro(), req.openChatUrl());
    }

    @Override
    public LicenseRes getLicense(String userEmail) {
        User user = findUser(userEmail);
        return driverLicenseRepository.findByOwnerId(user.getId())
                .map(dl -> new LicenseRes(dl.getLicenseNumber(), dl.getLicenseType(), dl.getIssuedAt(), dl.getExpiresAt(), dl.getName(), dl.getBirthDate(), dl.getAddress()))
                .orElse(null);
    }

    @Override
    @Transactional
    public void upsertLicense(String userEmail, LicenseReq req) {
        User user = findUser(userEmail);
        driverLicenseRepository.findByOwnerId(user.getId())
                .ifPresentOrElse(
                        dl -> dl.update(req.licenseNumber(), req.licenseType(), req.issuedAt(), req.expiresAt(), req.name(), req.birthDate(), req.address()),
                        () -> driverLicenseRepository.save(DriverLicense.builder()
                                .owner(user)
                                .licenseNumber(req.licenseNumber())
                                .licenseType(req.licenseType())
                                .issuedAt(req.issuedAt())
                                .expiresAt(req.expiresAt())
                                .name(req.name())
                                .birthDate(req.birthDate())
                                .address(req.address())
                                .build())
                );
        // flag
        user.updateProfileBasics(null, null, null, null, null);
        userRepository.save(user);
    }

    // ===== helpers =====
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private OngoingCarpoolRes toOngoingRes(Post p) {
        User author = p.getUser();
        return new OngoingCarpoolRes(
                p.getId(),
                p.getType(),
                p.getRoute().getFrom(),
                p.getRoute().getTo(),
                p.getDate(),
                p.getTime(),
                author.getProfileImg(),
                author.getNickname(),
                p.getMemo(),
                author.getTrustScore()
        );
    }

    private CompletedCarpoolRes toCompletedCarpoolRes(Post p) {
        User author = p.getUser();
        return new CompletedCarpoolRes(
                p.getId(),
                p.getType(),
                p.getRoute().getFrom(),
                p.getRoute().getTo(),
                p.getDate(),
                p.getTime(),
                author.getProfileImg(),
                author.getNickname(),
                author.getNickname(),
                p.getMemo(),
                author.getTrustScore()
        );
    }

    private OngoingCarpoolRes toOngoingResFromMatch(MatchMember mm) {
        Match m = mm.getMatch();
        User driver = m.getDriver();
        var driverPost = m.getDriverPost();
        String origin = m.getFromName() != null ? m.getFromName() : m.getFromKey();
        String dest = m.getToName() != null ? m.getToName() : m.getToKey();
        String memo = driverPost != null ? driverPost.getMemo() : null;
        Long postId = driverPost != null ? driverPost.getId() : null;
        return new OngoingCarpoolRes(
                postId,
                mm.getRole(),
                origin,
                dest,
                m.getDate(),
                m.getTime(),
                driver.getProfileImg(),
                driver.getNickname(),
                memo,
                driver.getTrustScore()
        );
    }

    private CompletedCarpoolRes toCompletedCarpoolResFromMatch(MatchMember mm) {
        Match m = mm.getMatch();
        User driver = m.getDriver();
        var driverPost = m.getDriverPost();
        String origin = m.getFromName() != null ? m.getFromName() : m.getFromKey();
        String dest = m.getToName() != null ? m.getToName() : m.getToKey();
        String memo = driverPost != null ? driverPost.getMemo() : null;
        Long postId = driverPost != null ? driverPost.getId() : null;
        return new CompletedCarpoolRes(
                postId,
                mm.getRole(),
                origin,
                dest,
                m.getDate(),
                m.getTime(),
                driver.getProfileImg(),
                driver.getNickname(),
                driver.getNickname(),
                memo,
                driver.getTrustScore()
        );
    }

    private MyPostRes toMyPostRes(Post p) {
        PostStatus status = calcStatusByNow(p.getDate(), p.getTime());
        User author = p.getUser();
        return new MyPostRes(
                p.getId(),
                p.getType(),
                status,
                p.getRoute().getFrom(),
                p.getRoute().getTo(),
                p.getDate(),
                p.getTime(),
                p.getSeats() != null ? p.getSeats() : 0,
                author.getProfileImg(),
                author.getNickname(),
                p.getMemo(),
                author.getTrustScore()
        );
    }

    private PostStatus calcStatusByNow(LocalDate date, LocalTime time) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (date.isAfter(today)) return PostStatus.RECRUITING;
        if (date.isBefore(today)) return PostStatus.COMPLETED;
        if (time.isAfter(now)) return PostStatus.RECRUITING;
        if (time.equals(now)) return PostStatus.ONGOING;
        return PostStatus.COMPLETED;
    }
}
