package com.bj.ilji_server.ilog.entity;

import com.bj.ilji_server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter // 1. Lombok: 모든 필드의 Getter를 자동으로 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 2. Lombok: 의미 없는 객체 생성을 막는 protected 기본 생성자
@Entity
@Table(name = "i_log",
    uniqueConstraints = {
        @UniqueConstraint( // 3. 한 유저가 같은 날짜에 중복 작성 방지
            name = "uq_i_log_user_date",
            columnNames = {"user_id", "i_log_date"}
        )
    }
)
public class ILog {
    
    @Id
    // 🚨 중요: Oracle은 IDENTITY 전략보다 SEQUENCE 전략이 훨씬 안정적이고 표준적입니다.
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "i_log_seq_generator")
    @SequenceGenerator(name = "i_log_seq_generator", sequenceName = "I_LOG_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // user_id 외래키 → users 테이블
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "i_log_date", nullable = false)
    private LocalDate iLogDate; // Java 표준 명명 규칙(camelCase) 적용

    // 🚨 중요: SQL의 CLOB 타입과 매핑하기 위해 @Lob 어노테이션이 필요합니다.
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "img_url", length = 2048) // URL 길이를 넉넉하게
    private String imgUrl;

    @CreationTimestamp // 4. JPA가 생성 시점에 자동으로 시간을 기록
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("0") // 5. DB에도 기본값 0을 명시
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; // 6. Null이 될 수 없으므로 primitive type(int)이 더 효율적

    @ColumnDefault("0")
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Builder // 7. Lombok: 빌더 패턴으로 객체를 안전하고 명확하게 생성
    public ILog(User user, LocalDate iLogDate, String content, String imgUrl) {
        this.user = user;
        this.iLogDate = iLogDate; // SQL의 i_log_date와 매핑
        this.content = content;
        this.imgUrl = imgUrl;
    }

    // 편의 메서드 예시
    public void increaseLike() {
        this.likeCount++;
    }
    public void decreaseLike() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseComment() {
        this.commentCount++;
    }

    public void decreaseComment() {
        // Math.max가 0 미만으로 내려가지 않도록 보장하므로 if문은 필요 없습니다.
        this.commentCount = Math.max(0, this.commentCount - 1);
    }
}
