DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS book_authors;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;
DROP TABLE IF EXISTS genres;
DROP TYPE IF EXISTS cover_enum;
DROP TYPE IF EXISTS order_status_enum;

----------------------------------------------------
-- 1. Создание ENUM типов
----------------------------------------------------

-- Тип для вида обложки
CREATE TYPE cover_t AS ENUM ('Твердая', 'Мягкая');

-- Тип для статуса заказа
CREATE TYPE status_t AS ENUM ('В обработке', 'Принят', 'Доставлен', 'Отменён');

----------------------------------------------------
-- 2. Создание таблиц
----------------------------------------------------

CREATE TABLE genres (
    id 		SERIAL PRIMARY KEY,
    name 	TEXT UNIQUE NOT NULL
);

CREATE TABLE authors (
    id 		SERIAL PRIMARY KEY,
    name 	TEXT NOT NULL
);

CREATE TABLE books (
    id 						SERIAL PRIMARY KEY,
    title 					TEXT NOT NULL,
    publisher 				TEXT,
    year_of_publication 	INT,
    pages 					INT,
    cover_type 				cover_t NOT NULL,
    price 					DECIMAL(10,2) NOT NULL,
    stock 					INT NOT NULL CHECK (stock >= 0)
);

CREATE TABLE book_authors (
    book_id 	INT NOT NULL,
    author_id 	INT NOT NULL,
	
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

CREATE TABLE book_genres (
    book_id 	INT NOT NULL,
    genre_id 	INT NOT NULL,
	
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE customers (
    id 			SERIAL PRIMARY KEY,
	username	TEXT NOT NULL UNIQUE,
	pwd_hash	TEXT NOT NULL,
    full_name 	TEXT NOT NULL,
    email 		TEXT UNIQUE NOT NULL,
    phone 		TEXT,
    address 	TEXT
);

CREATE TABLE orders (
    id 					SERIAL PRIMARY KEY,
    customer_id 		INT NOT NULL,
    order_date 			TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_price 		DECIMAL(10,2) NOT NULL,
    delivery_address 	TEXT NOT NULL,
    delivery_time 		TIMESTAMP,
    status 				status_t DEFAULT 'В обработке',
	
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE order_items (
    order_id 	INT NOT NULL,
    book_id 	INT NOT NULL,
    quantity 	INT NOT NULL CHECK (quantity > 0),
    price 		DECIMAL(10,2) NOT NULL CHECK (price > 0),
	
    PRIMARY KEY (order_id, book_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-----------------------------------------------------------
-- 3. Инициализация таблиц
-----------------------------------------------------------

INSERT INTO genres (name) VALUES 
    ('Антиутопия'),
    ('Роман'),
    ('Детектив'),
    ('Фантастика'),
    ('Научная литература');

INSERT INTO authors (name) VALUES 
    ('Джордж Оруэлл'),
    ('Михаил Булгаков'),
    ('Артур Конан Дойл'),
    ('Айзек Азимов'),
    ('Стивен Хокинг');

INSERT INTO books (title, publisher, year_of_publication, pages, cover_type, price, stock) VALUES 
    ('1984', 'Союз', 1949, 328, 'Твердая', 500.00, 10),
    ('Мастер и Маргарита', 'Азбука', 1966, 480, 'Мягкая', 450.00, 5),
    ('Приключения Шерлока Холмса', 'Penguin', 1892, 256, 'Твердая', 300.00, 7),
    ('Основание', 'HarperCollins', 1951, 255, 'Мягкая', 400.00, 12),
    ('Краткая история времени', 'Bantam Books', 1988, 212, 'Твердая', 350.00, 8);

INSERT INTO book_authors (book_id, author_id) VALUES 
    (1, 1), -- 1984 -> Джордж Оруэлл
    (2, 2), -- Мастер и Маргарита -> Михаил Булгаков
    (3, 3), -- Приключения Шерлока Холмса -> Артур Конан Дойл
    (4, 4), -- Основание -> Айзек Азимов
    (5, 5); -- Краткая история времени -> Стивен Хокинг

INSERT INTO book_genres (book_id, genre_id) VALUES 
    (1, 1), -- 1984 -> Антиутопия
    (2, 2), -- Мастер и Маргарита -> Роман
    (3, 3), -- Приключения Шерлока Холмса -> Детектив
    (4, 4), -- Основание -> Фантастика
    (5, 5); -- Краткая история времени -> Научная литература

INSERT INTO customers (full_name, email, phone, address) VALUES 
    ('Иван Иванов', 'ivan@example.com', '+79991234567', 'Москва, ул. Ленина, 1'),
    ('Мария Петрова', 'maria@example.com', '+79997654321', 'Санкт-Петербург, Невский пр., 10'),
    ('Алексей Смирнов', 'alex@example.com', '+79161234567', 'Казань, ул. Кремлевская, 5'),
    ('Ольга Кузнецова', 'olga@example.com', '+79161239876', 'Екатеринбург, ул. Мира, 3'),
    ('Сергей Волков', 'sergey@example.com', '+79161239999', 'Новосибирск, ул. Советская, 7');

INSERT INTO orders (customer_id, order_date, total_price, delivery_address, delivery_time, status) VALUES 
    (1, '2025-03-01 10:00:00', 500.00, 'Москва, ул. Ленина, 1', NULL, 'В обработке'),
    (2, '2025-03-02 11:00:00', 450.00, 'Санкт-Петербург, Невский пр., 10', NULL, 'Принят'),
    (3, '2025-03-03 12:00:00', 300.00, 'Казань, ул. Кремлевская, 5', '2025-03-04 16:00:00', 'Доставлен'),
    (4, '2025-03-04 13:00:00', 1000000.00, 'Екатеринбург, ул. Мира, 3', NULL, 'Отменён'),
    (5, '2025-03-05 14:00:00', 350.00, 'Новосибирск, ул. Советская, 7', NULL, 'Принят');

INSERT INTO order_items (order_id, book_id, quantity, price) VALUES 
    (1, 1, 1, 500.00),
    (2, 2, 1, 450.00),
    (3, 3, 1, 300.00),
    (4, 4, 2500, 400.00),
    (5, 5, 1, 350.00);