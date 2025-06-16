-- ADMIN 계정
INSERT INTO tbl_users (name, password, email, address, role, status, phone, birth_date,
                       registered_at, social_id, profile_image, social_provider)
VALUES ('admin',
        '$2a$10$OcOFA5fnT7Se7yp1jq3TpuIFnF37aCrY/VwFmTPnhyBhrEFl1AqH2',
        'admin@admin.com',
        '서울특별시 강남구',
        'ADMIN',
        'ACTIVE',
        '010-1111-2222',
        '2025-06-02',
        NOW(),
        NULL,
        NULL,
        NULL);

-- USER 계정
INSERT INTO tbl_users (name, password, email, address, role, status, phone, birth_date,
                       registered_at, social_id, profile_image, social_provider)
VALUES ('user',
        '$2a$10$TkoNtZgEO286HhW72WOr/eswnDRfg.dC.8ToiLdksy0vRAY4wVQny',
        'user@naver.com',
        '부산광역시 해운대구',
        'USER',
        'ACTIVE',
        '010-2222-3333',
        '2025-06-03',
        NOW(),
        NULL,
        NULL,
        NULL);

