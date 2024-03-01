-- Создание базы данных currency_exchange_app
CREATE DATABASE currency_exchange_app;

-- Подключение к созданной базе данных
\c currency_exchange_app;

-- Создание таблицы currencies
CREATE TABLE currencies
(
    id        SERIAL PRIMARY KEY,
    code      VARCHAR(3) UNIQUE NOT NULL,
    full_name VARCHAR,
    sign      VARCHAR(2)
);

-- Создание таблицы exchange_rates
CREATE TABLE exchange_rates
(
    id                 SERIAL PRIMARY KEY,
    base_currency_id   INTEGER,
    target_currency_id INTEGER,
    rate               DECIMAL(20, 6),
    CONSTRAINT fk_base_currency_id FOREIGN KEY (base_currency_id) REFERENCES currencies (id),
    CONSTRAINT fk_target_currency_id FOREIGN KEY (target_currency_id) REFERENCES currencies (id),
    CONSTRAINT base_target_id_unique UNIQUE (base_currency_id, target_currency_id)
);

-- Вставка данных в таблицу currencies
INSERT INTO currencies (code, full_name, sign)
VALUES ('USD', 'US Dollar', '$'),
       ('EUR', 'Euro', '€'),
       ('RUB', 'Russian ruble', '₽'),
       ('AUD', 'Australian dollar', 'A$');

-- Вставка данных в таблицу exchange_rates
INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
VALUES (1, 2, 0.92),
       (1, 3, 91.50),
       (1, 4, 1.54),
       (2, 3, 98.97),
       (2, 4, 1.66);
