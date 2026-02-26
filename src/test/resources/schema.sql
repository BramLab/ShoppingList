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
