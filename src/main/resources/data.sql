INSERT INTO members (id, member_id, password, display_name, credit_balance, created_at) VALUES
    (1, 'agent_maker', 'demo1234', '에이전트 메이커', 12, DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
    (2, 'demo_requester', 'demo1234', '데모 요청자', 10, DATEADD('DAY', -24, CURRENT_TIMESTAMP)),
    (3, 'low_credit', 'demo1234', '크레딧 부족 사용자', 1, DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
    (4, 'project_provider', 'demo1234', '프로젝트 빌더', 8, DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
    (5, 'experienced_user', 'demo1234', '참여 경험 사용자', 4, DATEADD('DAY', -12, CURRENT_TIMESTAMP));

INSERT INTO agent_offerings (
    id, provider_id, title, summary, listing_type, artifact_type, domain, environment,
    minimum_memory_gb, estimated_hours, credit_cost, capacity, available_slots,
    acceptance_criteria, license_name, status, completed_count, recommendation_count, created_at
) VALUES
    (101, 1, 'Spring Boot API Review Harness',
     'REST API의 계층 구조, 예외 응답과 트랜잭션 테스트를 검토하는 데모 Harness 구축 서비스입니다.',
     'BUILD_SERVICE', 'HARNESS', 'DEVELOPMENT', 'LOCAL', 8, 6, 4, 3, 3,
     '전체 테스트 결과와 실패 복구 체크리스트를 함께 제공합니다.', 'Demo License', 'OPEN', 2, 2,
     DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
    (102, 4, '데이터 품질 점검 Skill Pack',
     '결측치, 중복과 범위 오류를 순서대로 확인하는 데이터 분석용 데모 Skill 설계입니다.',
     'ASSET', 'SKILL', 'DATA', 'ANY', 4, 3, 3, 4, 4,
     '검사 항목별 결과와 재현 가능한 실행 순서를 문서로 제공합니다.', 'Demo License', 'OPEN', 1, 1,
     DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (103, 1, 'Harness Engineering 입문 워크숍',
     '도구 호출, 실패 처리, 권한과 합격 기준을 Agent Card로 정리하는 데모 교육 과정입니다.',
     'EDUCATION', 'HARNESS', 'EDUCATION', 'LOCAL', 4, 2, 2, 5, 5,
     '참여자가 하나의 Harness 구조와 검증 체크리스트를 완성합니다.', 'Demo Education License', 'OPEN', 1, 1,
     DATEADD('DAY', -8, CURRENT_TIMESTAMP)),
    (104, 4, 'Prompt Audit 프로젝트 베타 협업',
     '프롬프트 점검 서비스를 사용하고 오류 재현 절차와 사용성 피드백을 남기는 데모 프로젝트입니다.',
     'PROJECT', 'FULL_AGENT', 'DEVELOPMENT', 'CLOUD', 16, 10, 2, 4, 3,
     '재현 단계, 기대 결과와 실제 결과가 포함된 피드백을 제출합니다.', 'Demo Collaboration License', 'OPEN', 0, 0,
     DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
    (105, 1, '콘텐츠 작성 역할 지침 템플릿',
     '역할, 목적, 금지 사항과 출력 형식을 분리한 콘텐츠 업무용 데모 Instruction입니다.',
     'ASSET', 'INSTRUCTION', 'CONTENT', 'ANY', 2, 1, 1, 10, 10,
     '역할과 제약이 분리된 지침 예시와 확인 목록을 제공합니다.', 'Demo License', 'OPEN', 3, 2,
     DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
    (106, 4, 'Context 설계 실패 사례 가이드',
     '메모리, 외부 지식과 출력 형식을 과도하게 넣었을 때의 실패를 정리한 데모 지식 자료입니다.',
     'KNOWLEDGE', 'CONTEXT', 'BUSINESS', 'ANY', 2, 1, 1, 20, 20,
     '각 사례에 원인, 영향과 개선 원칙을 구분해 표시합니다.', 'Demo Knowledge License', 'OPEN', 4, 3,
     DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
    (107, 1, '운영 장애 복구 Loop',
     '상태 확인, 재시도, 중단과 사용자 전달 단계를 고정한 운영 자동화 데모 Loop입니다.',
     'ASSET', 'LOOP', 'OPERATIONS', 'HYBRID', 8, 4, 3, 1, 0,
     '각 단계의 중단 조건과 재시도 횟수를 명시합니다.', 'Demo License', 'CLOSED', 1, 1,
     DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
    (108, 4, '업무 요청 분류 Agent 구축',
     '업무 요청을 유형별로 분류하고 구조화된 결과를 만드는 데모 Full Agent 구축 서비스입니다.',
     'BUILD_SERVICE', 'FULL_AGENT', 'BUSINESS', 'CLOUD', 32, 20, 8, 2, 2,
     '정해진 예제 요청에서 분류 결과와 JSON 형식을 확인합니다.', 'Demo License', 'OPEN', 0, 0,
     DATEADD('DAY', -3, CURRENT_TIMESTAMP));

INSERT INTO agent_requests (
    id, requester_id, title, goal, request_type, desired_artifact_type, domain, environment,
    available_memory_gb, max_credits, expected_hours, constraints, acceptance_criteria, status, created_at
) VALUES
    (201, 2, 'Spring Boot API 검토 Harness가 필요합니다.',
     'Java 21 REST API의 트랜잭션과 예외 계약을 반복해서 확인할 구조가 필요합니다.',
     'BUILD_AGENT', 'HARNESS', 'DEVELOPMENT', 'LOCAL', 16, 5, 8,
     '외부 코드 업로드 없이 로컬에서 사용할 수 있어야 합니다.',
     '테스트 결과와 실패 복구 체크리스트가 포함되어야 합니다.', 'OPEN', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
    (202, 3, '낮은 크레딧으로 Harness 구축을 요청합니다.',
     '크레딧 부족 오류와 트랜잭션 롤백을 평가하기 위한 데모 요청입니다.',
     'BUILD_AGENT', 'HARNESS', 'DEVELOPMENT', 'LOCAL', 16, 5, 8,
     '외부 전송 없이 동작해야 합니다.',
     '참여 실패 뒤 잔액과 슬롯이 유지되어야 합니다.', 'OPEN', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
    (203, 5, 'Harness Engineering을 배우고 싶습니다.',
     '도구 호출과 실패 처리 기준을 작은 Agent Card로 정리하는 방법을 학습합니다.',
     'LEARN', 'HARNESS', 'EDUCATION', 'LOCAL', 8, 3, 4,
     '데모 데이터만 사용합니다.',
     'Harness 구조와 검증 체크리스트를 완성합니다.', 'CLOSED', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
    (204, 2, 'Prompt Audit 베타 테스트에 참여하고 싶습니다.',
     '실제 사용 흐름에서 발견한 오류를 재현 가능한 피드백으로 남깁니다.',
     'BETA_TEST', 'FULL_AGENT', 'DEVELOPMENT', 'CLOUD', 16, 3, 12,
     '가상 프로젝트와 데모 데이터만 사용합니다.',
     '재현 단계와 기대·실제 결과를 제출합니다.', 'OPEN', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (205, 5, 'Prompt Audit 공동 개발에 참여합니다.',
     '프로젝트의 오류 재현 양식과 검토 화면을 함께 개선합니다.',
     'CO_DEVELOP', 'FULL_AGENT', 'DEVELOPMENT', 'CLOUD', 16, 3, 12,
     '지분·고용 계약 없이 데모 협업만 수행합니다.',
     '합의한 데모 화면과 테스트 한 건을 완료합니다.', 'MATCHED', DATEADD('DAY', -1, CURRENT_TIMESTAMP));

INSERT INTO engagements (
    id, request_id, offering_id, requester_id, provider_id, credit_cost,
    compatibility_score, status, created_at, canceled_at, completed_at
) VALUES
    (301, 205, 104, 5, 4, 2, 100, 'ACTIVE', DATEADD('HOUR', -12, CURRENT_TIMESTAMP), NULL, NULL),
    (302, 203, 103, 5, 1, 2, 100, 'COMPLETED', DATEADD('DAY', -5, CURRENT_TIMESTAMP), NULL,
     DATEADD('DAY', -4, CURRENT_TIMESTAMP));

INSERT INTO recommendations (id, engagement_id, offering_id, member_id, comment, created_at) VALUES
    (401, 302, 103, 5, '실패 처리와 합격 기준을 함께 정리하는 실습이 유용했습니다.',
     DATEADD('DAY', -3, CURRENT_TIMESTAMP));

INSERT INTO credit_transactions (
    id, member_id, engagement_id, type, amount, balance_after, description, created_at
) VALUES
    (1001, 1, NULL, 'INITIAL', 10, 10, '초기 데모 크레딧', DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
    (1002, 2, NULL, 'INITIAL', 10, 10, '초기 데모 크레딧', DATEADD('DAY', -24, CURRENT_TIMESTAMP)),
    (1003, 3, NULL, 'INITIAL', 1, 1, '초기 데모 크레딧', DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
    (1004, 4, NULL, 'INITIAL', 8, 8, '초기 데모 크레딧', DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
    (1005, 5, NULL, 'INITIAL', 8, 8, '초기 데모 크레딧', DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
    (1006, 5, 302, 'ENGAGEMENT_USE', -2, 6, 'Agent 참여 크레딧 사용', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
    (1007, 1, 302, 'PROVIDER_REWARD', 2, 12, 'Agent 참여 완료 제공자 보상', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
    (1008, 5, 301, 'ENGAGEMENT_USE', -2, 4, 'Agent 참여 크레딧 사용', DATEADD('HOUR', -12, CURRENT_TIMESTAMP));

ALTER TABLE members ALTER COLUMN id RESTART WITH 10;
ALTER TABLE agent_offerings ALTER COLUMN id RESTART WITH 200;
ALTER TABLE agent_requests ALTER COLUMN id RESTART WITH 300;
ALTER TABLE engagements ALTER COLUMN id RESTART WITH 400;
ALTER TABLE recommendations ALTER COLUMN id RESTART WITH 500;
ALTER TABLE credit_transactions ALTER COLUMN id RESTART WITH 2000;

