package meet.myo.dto.request.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(name = "adoptNoticeCommentForm")
@Getter
public class AdoptNoticeCommentRequestDto {
    private Long noticeId;
    private String content;
}