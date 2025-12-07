package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * К╖┬Л²╢?≤Л²╢Л╖─?░Л└° ?╦Л╤°?≤К┼■ "?└Кё▄??Л╧╢М?" ??Й╠╢Л²≤ ?■Л∙╫ ?▒К▀╣ DTO.
 * - ?╛Л ╘Л╡? ЙЁ╪Й╠╟ Л╧╢М? ?╢Л≈╜(?└Кё▄ ???? К╙╘К║²/?│Л└╦ Л╧╢К⌠°
 * - ?╛М∙╗ ?∙КЁ╢: Й╡▄Л▀°Й╦─ ID, ????∙═, Л╤°К╟°/?└Л╟╘Л╖─, ?═Л╖°, ?°Й╟└
 */
public record CompletedCarpoolRes(\r
        Long matchId,       // ╦ед╙ ID\r
        Long postId,        // Й╡▄Л▀°Й╦─ ID
        PostRole myRole,    // ?╢К▀╧ Л╧╢М??░Л└° ?≤Л²≤ ??∙═ (?╢Л═└???? ?▒Л┼╧??
        String origin,      // Л╤°К╟°Л╖─
        String destination, // К╙╘Л═│Л╖─
        LocalDate date,     // Л╧╢М? ?═Л╖°
        LocalTime time,      // Л╧╢М? ?°Й╟└
        String profileImageUrl,
        String name,
        String nickname,
        String memo,
        Integer trustPoint
) { }

