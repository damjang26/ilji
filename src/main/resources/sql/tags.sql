UPDATE tags
SET visibility = 'PUBLIC'
WHERE id = 65;

-- 66번 태그를 'MUTUAL_FRIENDS'로 변경
UPDATE tags
SET visibility = 'MUTUAL_FRIENDS'
    WHERE id = 66;

-- 변경사항 최종 저장
COMMIT;

select * from TAGS;
