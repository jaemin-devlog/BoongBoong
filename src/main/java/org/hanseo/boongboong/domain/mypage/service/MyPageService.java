package org.hanseo.boongboong.domain.mypage.service;

import org.hanseo.boongboong.domain.mypage.dto.response.*;

import java.util.List;

public interface MyPageService {

    MyPageRes getMyPageInfo(String userEmail);
    ProfileCardRes profile(String userEmail);
    OngoingCarpoolRes ongoing(String userEmail);
    OngoingCarpoolRes upcoming(String userEmail);
    List<MyPostRes> myPosts(String userEmail);

    ProfileCardRes upsertVehicle(String userEmail, String number, String imageUrl, Integer seats, String color);
    void deleteVehicle(String userEmail);

    List<OngoingCarpoolRes> upcomingList(String userEmail);
    List<CompletedCarpoolRes> completedList(String userEmail);

    void updateProfile(String userEmail, String name, Integer age, String profileImg, String intro, String openChatUrl);

    // Driver license
    org.hanseo.boongboong.domain.mypage.dto.response.LicenseRes getLicense(String userEmail);
    void upsertLicense(String userEmail, org.hanseo.boongboong.domain.mypage.dto.request.LicenseReq req);

    // One-shot save for full profile page
    void saveFullProfile(String userEmail, org.hanseo.boongboong.domain.mypage.dto.request.FullProfileUpdateReq req);
}
