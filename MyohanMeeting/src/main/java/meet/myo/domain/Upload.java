package meet.myo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Upload extends BaseAuditingListener {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Long id;

    @Size(max = 512)
    @Column(nullable = false)
    private String url;

    @Size(max = 512)
    @Column(nullable = false)
    private String path;

    private String originName;

    @Column(nullable = false)
    private String savedName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private Long size;

//    TODO: 필드 수가 많고, 모두 스트링 필드이므로 생성 시 혼동이 생길 수 있을 것 같아 빌더 패턴을 적용했습니다.
    @Builder
    public Upload(String url, String path, String originName, String savedName, String type, String extension, Long size) {
        this.url = url;
        this.path = path;
        this.originName = originName;
        this.savedName = savedName;
        this.type = type;
        this.extension = extension;
        this.size = size;
    }
}