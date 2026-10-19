-- SafeSip database schema and sample data (MySQL)
CREATE DATABASE IF NOT EXISTS safesip;
USE safesip;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  role ENUM('ADMIN','STAFF','CUSTOMER') NOT NULL,
  age INT,
  weight_kg DECIMAL(5,2),
  gender ENUM('M','F')
);

CREATE TABLE products (
  product_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  price DECIMAL(8,2) NOT NULL,
  alcohol_percent DECIMAL(4,1) NOT NULL,
  volume_ml INT NOT NULL
);

CREATE TABLE inventory (
  product_id INT PRIMARY KEY,
  quantity INT NOT NULL,
  FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE consumption_records (
  record_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  consumed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES users(user_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE sales (
  sale_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  staff_id INT NOT NULL,
  total DECIMAL(10,2),
  sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES users(user_id),
  FOREIGN KEY (staff_id) REFERENCES users(user_id)
);

CREATE TABLE sale_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  sale_id INT NOT NULL,
  product_id INT NOT NULL,
  qty INT NOT NULL,
  price DECIMAL(8,2) NOT NULL,
  FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Sample data
INSERT INTO users (name, username, password, role, age, weight_kg, gender) VALUES
('Admin','admin','admin123','ADMIN',30,70,'M'),
('Staff One','staff1','staff123','STAFF',28,65,'F'),
('Ravi','ravi','ravi123','CUSTOMER',25,72,'M');

INSERT INTO products (name, category, price, alcohol_percent, volume_ml) VALUES
('Lager Beer','Beer',150,5.0,650),
('Red Wine','Wine',900,12.5,750),
('Whisky','Spirits',1800,42.8,750);

INSERT INTO inventory VALUES (1,50),(2,30),(3,20);
