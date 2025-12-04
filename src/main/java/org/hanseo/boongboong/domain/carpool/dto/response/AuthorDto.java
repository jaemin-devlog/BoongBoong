package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;

// ??• : ?‘ì„±???•ë³´ë¥?API ?‘ë‹µ?¼ë¡œ ?„ë‹¬?˜ëŠ” ê°€ë²¼ìš´ DTO (id, ?‰ë„¤?? ?„ë¡œ???´ë?ì§€)
// ?¬ìš©ì²? Carpool ?‘ë‹µ ë°”ë””?ì„œ ?‘ì„±??ë¸”ë¡???½ì…
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorDto {
    private Long id;          // ?‘ì„±??ID
    private String nick;      // ?‘ì„±???‰ë„¤???œë¹„???¸ì¶œëª?
    private String profileImg;
    private int trustScore;    // ½Å·Ú Á¡¼ö // ?„ë¡œ???´ë?ì§€(Data URL ?ëŠ” URL)
}

