/**
 * 마이페이지 유스케이스 인터페이스.
 * - 컨트롤러가 의존하는 서비스 계약을 정의
 */
package org.hanseo.boongboong.domain.mypage.service;

import org.hanseo.boongboong.domain.mypage.dto.response.*;

import java.util.List;

public interface MyPageService {

    MyPageRes getMyPageInfo(String userEmail); // 메인 카드/목록 집합 조회
    ProfileCardRes profile(String userEmail); // 프로필 카드 조회
    OngoingCarpoolRes ongoing(String userEmail); // 진행중 1건(매칭 도입 전까지 null)
    OngoingCarpoolRes upcoming(String userEmail); // 다가올(예정) 1건
    List<MyPostRes> myPosts(String userEmail); // 내가 올린 글 최신 50
    ProfileCardRes upsertVehicle(String userEmail, String number, String imageUrl); // 차량 업서트
    void deleteVehicle(String userEmail); // 차량 삭제
    List<OngoingCarpoolRes> upcomingList(String userEmail); // 다가올 전체 리스트
}
