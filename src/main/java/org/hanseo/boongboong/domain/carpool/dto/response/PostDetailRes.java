package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.vehicle.entity.Vehicle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class PostDetailRes {

    // Post Info
    private final Long postId;
    private final PostRole type;
    private final LocalDate date;
    private final LocalTime time;
    private final String from;
    private final String to;
    private final String memo;

    // Author Info
    private final AuthorInfo author;

    // Vehicle Info (Nullable)
    private final VehicleInfo vehicle;

    // Review Info (Not implemented yet)
    private final List<ReviewInfo> reviews;

    @Getter
    @Builder
    private static class AuthorInfo {
        private final Long userId;
        private final String nickname;
        private final String profileImageUrl;
        private final int trustScore;

        public static AuthorInfo from(User user) {
            return AuthorInfo.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImg())
                    .trustScore(user.getTrustScore())
                    .build();
        }
    }

    @Getter
    @Builder
    private static class VehicleInfo {
        private final String carNumber;
        private final String carImageUrl;
        private final Integer seats;

        public static VehicleInfo of(Vehicle vehicle, Integer seats) {
            return VehicleInfo.builder()
                    .carNumber(vehicle.getNumber())
                    .carImageUrl(vehicle.getImageUrl())
                    .seats(seats)
                    .build();
        }
    }

    @Getter
    @Builder
    private static class ReviewInfo {
        // Not implemented yet
    }

    public static PostDetailRes from(Post post, Vehicle vehicle) {
        User author = post.getUser();

        VehicleInfo vehicleInfo = null;
        if (post.getType() == PostRole.DRIVER && vehicle != null) {
            vehicleInfo = VehicleInfo.of(vehicle, post.getSeats());
        }

        return PostDetailRes.builder()
                .postId(post.getId())
                .type(post.getType())
                .date(post.getDate())
                .time(post.getTime())
                .from(post.getRoute().getFrom())
                .to(post.getRoute().getTo())
                .memo(post.getMemo())
                .author(AuthorInfo.from(author))
                .vehicle(vehicleInfo)
                .reviews(Collections.emptyList()) // 리뷰는 아직 미구현
                .build();
    }
}
