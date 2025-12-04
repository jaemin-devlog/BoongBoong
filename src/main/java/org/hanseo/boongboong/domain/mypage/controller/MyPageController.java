package org.hanseo.boongboong.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.mypage.dto.response.*;
import org.hanseo.boongboong.domain.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/me")
    public ResponseEntity<MyPageRes> me(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.getMyPageInfo(email));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileCardRes> profile(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.profile(email));
    }

    @GetMapping("/ongoing")
    public ResponseEntity<OngoingCarpoolRes> ongoing(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.ongoing(email));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<OngoingCarpoolRes> upcoming(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.upcoming(email));
    }

    @GetMapping("/upcoming/list")
    public ResponseEntity<List<OngoingCarpoolRes>> upcomingList(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.upcomingList(email));
    }

    @GetMapping("/completed/list")
    public ResponseEntity<List<CompletedCarpoolRes>> completedList(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.completedList(email));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<MyPostRes>> myPosts(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.myPosts(email));
    }

    @PostMapping("/car")
    public ResponseEntity<ProfileCardRes> upsertVehicle(@RequestParam String email,
                                                        @RequestBody VehicleReq req) {
        return ResponseEntity.ok(myPageService.upsertVehicle(email, req.number(), req.imageUrl(), req.seats(), req.color()));
    }

    @DeleteMapping("/car")
    public ResponseEntity<Void> deleteVehicle(@RequestParam String email) {
        myPageService.deleteVehicle(email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> saveFullProfile(@RequestParam String email,
                                                @RequestBody org.hanseo.boongboong.domain.mypage.dto.request.FullProfileUpdateReq req) {
        myPageService.saveFullProfile(email, req);
        return ResponseEntity.ok().build();
    }

    public record VehicleReq(String number, String imageUrl, Integer seats, String color) {}

    @GetMapping("/license")
    public ResponseEntity<org.hanseo.boongboong.domain.mypage.dto.response.LicenseRes> getLicense(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.getLicense(email));
    }

    @PostMapping("/license")
    public ResponseEntity<Void> upsertLicense(@RequestParam String email,
                                              @RequestBody org.hanseo.boongboong.domain.mypage.dto.request.LicenseReq req) {
        myPageService.upsertLicense(email, req);
        return ResponseEntity.ok().build();
    }
}
