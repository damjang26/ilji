CREATE TABLE USER_PROFILE (
                              USER_ID             NUMBER PRIMARY KEY,                              -- 로그인된 유저의 ID (PK, FK)
                              NICKNAME            VARCHAR2(50) DEFAULT NULL,                       -- 닉네임 (입력 없을 시 USERS.name 사용)
                              BIRTHDATE           DATE,                                            -- 생일 (예: 생일 일정 자동 생성용)
                              PHONE_NUMBER        VARCHAR2(20),                                    -- SNS 등 연락처
                              PROFILE_IMAGE       VARCHAR2(255),                                   -- 프로필 이미지 URL (기본값: USERS.picture)
                              BANNER_IMAGE        VARCHAR2(255) DEFAULT 'https://default-banner.com/image.jpg', -- 마이페이지 배너
                              GENDER              VARCHAR2(10),                                    -- 'M', 'F', 'Other' 등
                              REGION              VARCHAR2(100),                                   -- 예: 'Seoul', 'Busan'
                              BIO                 VARCHAR2(255),                                   -- 상태 메시지
                              IS_PRIVATE          NUMBER(1) DEFAULT 0 CHECK (IS_PRIVATE IN (0,1)), -- 계정 비공개 여부 (0: 공개, 1: 비공개)
                              INTERESTS           VARCHAR2(500),                                   -- 관심사 (쉼표 구분 문자열, 예: '운동,음악,여행')
                              CREATED_AT          DATE DEFAULT SYSDATE,
                              UPDATED_AT          DATE,

                              CONSTRAINT FK_USER_PROFILE_USER
                                  FOREIGN KEY (USER_ID)
                                      REFERENCES USERS(ID)
                                          ON DELETE CASCADE
);


-- 예: 유저 ID 1번, 일부 필드만 입력
INSERT INTO USER_PROFILE (
    USER_ID, NICKNAME, BIRTHDATE, PHONE_NUMBER, GENDER, REGION, BIO, IS_PRIVATE, INTERESTS
) VALUES (
             1, NULL, TO_DATE('1995-12-01', 'YYYY-MM-DD'), '010-2222-3333', 'M', 'Seoul', '8월 내내 휴가입니다~', 0, '음악,코딩,야구'
         );

-- 예: 유저 ID 2번, 전부 수동 입력
INSERT INTO USER_PROFILE (
    USER_ID, NICKNAME, BIRTHDATE, PHONE_NUMBER, PROFILE_IMAGE, BANNER_IMAGE, GENDER, REGION, BIO, IS_PRIVATE, INTERESTS
) VALUES (
             2, 'sunnygirl', TO_DATE('1990-06-20', 'YYYY-MM-DD'), '010-9999-8888',
             'https://example.com/profile2.jpg',
             'https://example.com/banner2.jpg',
             'F', 'Busan', '9월 한 달간 연락 불가', 1, '여행,요리,자연'
         );


SELECT * from USER_PROFILE
