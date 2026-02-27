/* # (number sign, hash, pound sign, hashtag) is NOT RECOGNISED as comment in H2! */
/* H2 comment */
// H2 comment
-- H2 comment

CREATE TABLE IF NOT EXISTS storage (
                                       id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name    VARCHAR(255),
                                       remarks VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS food (
                                    created_at TIMESTAMP      NULL,
                                    deleted_at TIMESTAMP      NULL,
                                    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    updated_at TIMESTAMP      NULL,
                                    dtype      VARCHAR(31)    NOT NULL,
                                    name       VARCHAR(255)   NULL,
                                    remarks    VARCHAR(255)   NULL
);

CREATE TABLE IF NOT EXISTS food_original (
                                             best_before_end DATE   NULL,
                                             original_ml_g   DOUBLE NOT NULL,
                                             remaining_ml_g  DOUBLE NOT NULL,
                                             use_by          DATE   NULL,
                                             food_id         BIGINT NOT NULL PRIMARY KEY,
                                             CONSTRAINT FK4a1tyhabf5gokqhlqgxtaomb8
                                                 FOREIGN KEY (food_id) REFERENCES food (id)
);

CREATE TABLE IF NOT EXISTS user_home (
                                         created_at TIMESTAMP   NULL,
                                         updated_at TIMESTAMP   NULL,
                                         id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         name       VARCHAR(255)
);

// USER is a reserved keyword in H2, quote is quick-fix
CREATE TABLE IF NOT EXISTS "user" (
                                    created_at      TIMESTAMP    NULL,
                                    updated_at      TIMESTAMP    NULL,
                                    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    username        VARCHAR(255),
                                    email           VARCHAR(255),
                                    user_role       VARCHAR(50),
                                    password_hashed VARCHAR(255),
                                    user_home_id    BIGINT,
                                    FOREIGN KEY (user_home_id) REFERENCES user_home (id)
);

CREATE TABLE IF NOT EXISTS storage_type (
                                            id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            name    VARCHAR(255),
                                            remarks VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS stored_food (
                                           created_at       TIMESTAMP NULL,
                                           updated_at       TIMESTAMP NULL,
                                           id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           quantity         INT,
                                           food_id          BIGINT,
                                           storage_type_id  BIGINT,
                                           user_home_id     BIGINT,
                                           FOREIGN KEY (food_id)         REFERENCES food (id),
                                           FOREIGN KEY (storage_type_id) REFERENCES storage_type (id),
                                           FOREIGN KEY (user_home_id)    REFERENCES user_home (id)
);
