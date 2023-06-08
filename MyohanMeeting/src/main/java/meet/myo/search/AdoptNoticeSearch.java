package meet.myo.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Builder
public class AdoptNoticeSearch {

    // 공고 정보
    private String title;
    private String content;
    private String authorName;
    private String noticeStatus;

    // 고양이 정보
    private String catName;
    private String catSpecies;
    private String catAge;
    private String catSex;
    private String neutered;
    private String healthStatus;
    private String personality;
    private String foundedPlace;
    private String foundedAt;

    // 보호소 정보
    private String city;
    private String shelterName;

    // 정렬 기준
    private String ordered;
}
