# 📚 Mô Tả Database - BookStore Microservices

> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 07/01/2026  
> **Kiến trúc:** Database per Service Pattern

---

## 📋 Mục Lục

1. [Tổng Quan Hệ Thống](#1-tổng-quan-hệ-thống)
2. [Identity Database](#2-identity-database)
3. [Product Database](#3-product-database)
4. [Order Database](#4-order-database)
5. [Inventory Database](#5-inventory-database)
6. [Payment Database](#6-payment-database)
7. [Promotion Database](#7-promotion-database)
8. [Notification Database](#8-notification-database)
9. [Quan Hệ Giữa Các Service](#9-quan-hệ-giữa-các-service)
10. [Quy Ước Chung](#10-quy-ước-chung)

---

## 1. Tổng Quan Hệ Thống

### 1.1 Kiến Trúc Database

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BOOK STORE MICROSERVICES                             │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────────────────┤
│ identity_db │ product_db  │  order_db   │inventory_db │     payment_db      │
│  (6 bảng)   │  (11 bảng)  │  (5 bảng)   │  (4 bảng)   │     (2 bảng)        │
├─────────────┴─────────────┴─────────────┴─────────────┴─────────────────────┤
│                    promotion_db (4 bảng)  │  notification_db (3 bảng)       │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Thống Kê Tổng Quan

| Database | Số Bảng | Mục Đích |
|----------|---------|----------|
| `identity_db` | 6 | Xác thực, phân quyền, quản lý người dùng |
| `product_db` | 11 | Quản lý sản phẩm, danh mục, tác giả, đánh giá |
| `order_db` | 5 | Giỏ hàng, đơn hàng |
| `inventory_db` | 4 | Quản lý kho, tồn kho |
| `payment_db` | 2 | Thanh toán, hoàn tiền |
| `promotion_db` | 4 | Mã giảm giá, flash sale |
| `notification_db` | 3 | Thông báo, email, SMS |
| **Tổng** | **35** | |

---

## 2. Identity Database

> **Mục đích:** Quản lý xác thực, phân quyền và thông tin người dùng

### 2.1 Sơ Đồ ERD

```
┌──────────────────────┐
│        users         │
├──────────────────────┤
│ PK id                │
│    username          │
│    email             │
│    password          │
│    full_name         │
│    phone_number      │
│    avatar_url        │
│    role              │
│    enabled           │
│    email_verified    │
│    phone_verified    │
│    locked            │
│    lock_reason       │
│    failed_login_attempts│
│    last_login_at     │
│    created_at        │
│    updated_at        │
└──────────┬───────────┘
           │
           │ 1:N
           ▼
┌──────────────────────┐     ┌──────────────────────┐     ┌──────────────────────┐
│   user_addresses     │     │   refresh_tokens     │     │ password_reset_tokens│
├──────────────────────┤     ├──────────────────────┤     ├──────────────────────┤
│ PK id                │     │ PK id                │     │ PK id                │
│ FK user_id           │     │ FK user_id           │     │ FK user_id           │
│    address_type      │     │    token             │     │    token             │
│    recipient_name    │     │    device_info       │     │    expires_at        │
│    phone_number      │     │    ip_address        │     │    used              │
│    province          │     │    expires_at        │     │    created_at        │
│    district          │     │    revoked           │     └──────────────────────┘
│    ward              │     │    created_at        │
│    street_address    │     └──────────────────────┘
│    postal_code       │
│    is_default        │     ┌──────────────────────┐     ┌──────────────────────┐
│    created_at        │     │email_verification    │     │  user_login_history  │
│    updated_at        │     │      _tokens         │     ├──────────────────────┤
└──────────────────────┘     ├──────────────────────┤     │ PK id                │
                             │ PK id                │     │ FK user_id           │
                             │ FK user_id           │     │    login_at          │
                             │    token             │     │    ip_address        │
                             │    expires_at        │     │    user_agent        │
                             │    verified_at       │     │    device_type       │
                             │    created_at        │     │    login_status      │
                             └──────────────────────┘     │    failure_reason    │
                                                          └──────────────────────┘
```

### 2.2 Chi Tiết Bảng

#### 2.2.1 `users` - Người dùng

| Cột | Kiểu Dữ Liệu | Null | Mô Tả |
|-----|--------------|------|-------|
| `id` | BIGINT | NO | Khóa chính, tự tăng |
| `username` | VARCHAR(50) | NO | Tên đăng nhập, duy nhất |
| `email` | VARCHAR(100) | NO | Email, duy nhất |
| `password` | VARCHAR(255) | NO | Mật khẩu đã mã hóa (BCrypt) |
| `full_name` | VARCHAR(100) | YES | Họ tên đầy đủ |
| `phone_number` | VARCHAR(20) | YES | Số điện thoại |
| `avatar_url` | VARCHAR(500) | YES | URL ảnh đại diện |
| `role` | ENUM | NO | Vai trò: CUSTOMER, ADMIN, STAFF |
| `enabled` | BOOLEAN | NO | Tài khoản còn hoạt động không |
| `email_verified` | BOOLEAN | NO | Email đã xác thực chưa |
| `phone_verified` | BOOLEAN | NO | SĐT đã xác thực chưa |
| `locked` | BOOLEAN | NO | Tài khoản bị khóa không |
| `lock_reason` | VARCHAR(255) | YES | Lý do khóa tài khoản |
| `failed_login_attempts` | INT | YES | Số lần đăng nhập sai |
| `last_login_at` | DATETIME | YES | Thời điểm đăng nhập cuối |
| `created_at` | DATETIME | NO | Thời điểm tạo |
| `updated_at` | DATETIME | YES | Thời điểm cập nhật |

**Indexes:**
- `idx_users_email` ON (email)
- `idx_users_username` ON (username)
- `idx_users_role` ON (role)
- `idx_users_enabled` ON (enabled)

#### 2.2.2 `user_addresses` - Địa chỉ người dùng

| Cột | Kiểu Dữ Liệu | Null | Mô Tả |
|-----|--------------|------|-------|
| `id` | BIGINT | NO | Khóa chính |
| `user_id` | BIGINT | NO | FK → users.id |
| `address_type` | ENUM | NO | SHIPPING, BILLING, BOTH |
| `recipient_name` | VARCHAR(100) | NO | Tên người nhận |
| `phone_number` | VARCHAR(20) | NO | SĐT người nhận |
| `province` | VARCHAR(100) | NO | Tỉnh/Thành phố |
| `district` | VARCHAR(100) | NO | Quận/Huyện |
| `ward` | VARCHAR(100) | YES | Phường/Xã |
| `street_address` | VARCHAR(255) | NO | Địa chỉ chi tiết |
| `postal_code` | VARCHAR(20) | YES | Mã bưu chính |
| `is_default` | BOOLEAN | NO | Địa chỉ mặc định |

#### 2.2.3 `refresh_tokens` - Token làm mới

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `user_id` | BIGINT | FK → users.id |
| `token` | VARCHAR(500) | Token JWT |
| `device_info` | VARCHAR(255) | Thông tin thiết bị |
| `ip_address` | VARCHAR(45) | Địa chỉ IP |
| `expires_at` | DATETIME | Thời điểm hết hạn |
| `revoked` | BOOLEAN | Đã thu hồi chưa |

---

## 3. Product Database

> **Mục đích:** Quản lý catalog sản phẩm, danh mục, tác giả, đánh giá

### 3.1 Sơ Đồ ERD

```
                              ┌──────────────────────┐
                              │     categories       │
                              ├──────────────────────┤
                              │ PK id                │
                              │ FK parent_id (self)  │◄────┐
                              │    name              │     │
                              │    slug              │     │ Self-reference
                              │    description       │     │ (Hierarchical)
                              │    image_url         │     │
                              │    display_order     │─────┘
                              │    is_active         │
                              └──────────┬───────────┘
                                         │
                                         │ 1:N
                                         ▼
┌──────────────────────┐    ┌──────────────────────────────────────────────────────┐
│     publishers       │    │                      products                         │
├──────────────────────┤    ├──────────────────────────────────────────────────────┤
│ PK id                │    │ PK id                                                 │
│    name              │◄───│ FK category_id, publisher_id                          │
│    slug              │    │    sku, isbn, title, slug                             │
│    description       │    │    description, short_description                     │
│    logo_url          │    │    original_price, selling_price, discount_percent    │
│    website           │    │    publication_date, language, page_count             │
│    address           │    │    weight, dimensions, cover_type, thumbnail_url      │
└──────────────────────┘    │    status, is_featured, is_bestseller, is_new_arrival │
                            │    meta_title, meta_description, meta_keywords        │
┌──────────────────────┐    │    view_count, sold_count, rating_average, rating_count│
│       authors        │    └────────────────────────┬─────────────────────────────┘
├──────────────────────┤                             │
│ PK id                │                             │ 1:N
│    name              │    ┌────────────────────────┼────────────────────────┐
│    slug              │    │                        │                        │
│    biography         │    ▼                        ▼                        ▼
│    avatar_url        │  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│    birth_date        │  │ product_images │  │product_reviews │  │   wishlists    │
│    nationality       │  ├────────────────┤  ├────────────────┤  ├────────────────┤
│    website           │  │ PK id          │  │ PK id          │  │ PK id          │
└──────────┬───────────┘  │ FK product_id  │  │ FK product_id  │  │    user_id     │
           │              │    image_url   │  │    user_id     │  │ FK product_id  │
           │              │    alt_text    │  │    order_item_id│ │    added_at    │
           │ N:M          │    display_order│ │    rating      │  └────────────────┘
           │              │    is_primary  │  │    title       │
           ▼              └────────────────┘  │    content     │
┌──────────────────────┐                      │    pros, cons  │
│   product_authors    │                      │    is_verified │
├──────────────────────┤                      │    is_approved │
│ PK product_id        │                      │    helpful_count│
│ PK author_id         │                      └────────┬───────┘
│    author_role       │                               │
└──────────────────────┘                               │ 1:N
                                                       ▼
                                              ┌────────────────────┐
                                              │product_review_images│
                                              ├────────────────────┤
                                              │ PK id              │
                                              │ FK review_id       │
                                              │    image_url       │
                                              └────────────────────┘
```

### 3.2 Chi Tiết Bảng

#### 3.2.1 `products` - Sản phẩm (Sách)

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `sku` | VARCHAR(50) | Mã sản phẩm, duy nhất |
| `isbn` | VARCHAR(20) | Mã ISBN sách |
| `title` | VARCHAR(255) | Tên sách |
| `slug` | VARCHAR(280) | URL slug, duy nhất |
| `description` | TEXT | Mô tả chi tiết |
| `short_description` | VARCHAR(500) | Mô tả ngắn |
| `original_price` | DECIMAL(12,2) | Giá gốc |
| `selling_price` | DECIMAL(12,2) | Giá bán |
| `discount_percent` | DECIMAL(5,2) | % giảm giá |
| `category_id` | BIGINT | FK → categories.id |
| `publisher_id` | BIGINT | FK → publishers.id |
| `publication_date` | DATE | Ngày xuất bản |
| `language` | VARCHAR(50) | Ngôn ngữ (mặc định: Tiếng Việt) |
| `page_count` | INT | Số trang |
| `weight` | INT | Trọng lượng (gram) |
| `dimensions` | VARCHAR(50) | Kích thước (VD: 20x15x2 cm) |
| `cover_type` | ENUM | PAPERBACK, HARDCOVER, EBOOK |
| `thumbnail_url` | VARCHAR(500) | URL ảnh bìa |
| `status` | ENUM | ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED |
| `is_featured` | BOOLEAN | Sản phẩm nổi bật |
| `is_bestseller` | BOOLEAN | Sản phẩm bán chạy |
| `is_new_arrival` | BOOLEAN | Sản phẩm mới |
| `view_count` | BIGINT | Lượt xem |
| `sold_count` | BIGINT | Số lượng đã bán |
| `rating_average` | DECIMAL(2,1) | Điểm đánh giá trung bình |
| `rating_count` | INT | Số lượng đánh giá |

**Indexes:**
- `idx_products_sku`, `idx_products_isbn`, `idx_products_slug`
- `idx_products_category`, `idx_products_status`, `idx_products_price`
- `idx_products_featured`, `idx_products_bestseller`

#### 3.2.2 `categories` - Danh mục

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(100) | Tên danh mục, duy nhất |
| `slug` | VARCHAR(120) | URL slug, duy nhất |
| `description` | TEXT | Mô tả |
| `image_url` | VARCHAR(500) | Ảnh danh mục |
| `parent_id` | BIGINT | FK → categories.id (tự tham chiếu) |
| `display_order` | INT | Thứ tự hiển thị |
| `is_active` | BOOLEAN | Đang hoạt động |

**Lưu ý:** Hỗ trợ danh mục đa cấp (hierarchical) thông qua `parent_id`

#### 3.2.3 `product_reviews` - Đánh giá sản phẩm

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `product_id` | BIGINT | FK → products.id |
| `user_id` | BIGINT | ID người dùng (cross-service) |
| `order_item_id` | BIGINT | ID order item (verified purchase) |
| `rating` | INT | Điểm đánh giá (1-5) |
| `title` | VARCHAR(255) | Tiêu đề đánh giá |
| `content` | TEXT | Nội dung đánh giá |
| `pros` | TEXT | Ưu điểm |
| `cons` | TEXT | Nhược điểm |
| `is_verified_purchase` | BOOLEAN | Đã mua hàng thật |
| `is_approved` | BOOLEAN | Đã được duyệt |
| `helpful_count` | INT | Số lượt hữu ích |

**Unique Constraint:** (product_id, user_id) - Mỗi user chỉ đánh giá 1 lần/sản phẩm

---

## 4. Order Database

> **Mục đích:** Quản lý giỏ hàng và đơn hàng

### 4.1 Sơ Đồ ERD

```
┌──────────────────────┐          ┌──────────────────────────────────────────────┐
│        carts         │          │                    orders                     │
├──────────────────────┤          ├──────────────────────────────────────────────┤
│ PK id                │          │ PK id                                         │
│    user_id           │          │    order_number                               │
│    session_id        │          │    user_id                                    │
│    status            │          │    status                                     │
│    currency          │          │    currency, subtotal, shipping_fee           │
│    subtotal          │          │    discount_amount, tax_amount, total_amount  │
│    discount_amount   │          │    coupon_id, coupon_code                     │
│    total             │          │    shipping_recipient_name, shipping_phone    │
│    coupon_code       │          │    shipping_province, district, ward, address │
│    expires_at        │          │    shipping_method, shipping_carrier          │
└──────────┬───────────┘          │    tracking_number, estimated_delivery_date   │
           │                      │    payment_method, payment_status, paid_at    │
           │ 1:N                  │    customer_note, admin_note, cancel_reason   │
           ▼                      └────────────────────────┬───────────────────────┘
┌──────────────────────┐                                   │
│     cart_items       │                                   │ 1:N
├──────────────────────┤          ┌────────────────────────┼────────────────────────┐
│ PK id                │          │                        │                        │
│ FK cart_id           │          ▼                        ▼                        ▼
│    product_id        │   ┌──────────────────┐   ┌──────────────────────┐
│    quantity          │   │   order_items    │   │ order_status_history │
│    unit_price        │   ├──────────────────┤   ├──────────────────────┤
│    subtotal          │   │ PK id            │   │ PK id                │
└──────────────────────┘   │ FK order_id      │   │ FK order_id          │
                           │    product_id    │   │    from_status       │
                           │    product_sku   │   │    to_status         │
                           │    product_name  │   │    notes             │
                           │    product_image │   │    changed_by        │
                           │    quantity      │   │    created_at        │
                           │    unit_price    │   └──────────────────────┘
                           │    discount_amount│
                           │    subtotal      │
                           │    returned_qty  │
                           │    refunded_amount│
                           └──────────────────┘
```

### 4.2 Chi Tiết Bảng

#### 4.2.1 `orders` - Đơn hàng

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `order_number` | VARCHAR(30) | Mã đơn hàng (VD: ORD-20260107-0001) |
| `user_id` | BIGINT | ID người dùng |
| `status` | ENUM | Trạng thái đơn hàng |
| `currency` | VARCHAR(3) | Đơn vị tiền tệ (VND) |
| `subtotal` | DECIMAL(15,2) | Tổng giá sản phẩm |
| `shipping_fee` | DECIMAL(12,2) | Phí vận chuyển |
| `discount_amount` | DECIMAL(12,2) | Số tiền được giảm |
| `tax_amount` | DECIMAL(12,2) | Thuế |
| `total_amount` | DECIMAL(15,2) | Tổng thanh toán |
| `coupon_id` | BIGINT | ID mã giảm giá |
| `coupon_code` | VARCHAR(50) | Mã giảm giá |
| `payment_method` | ENUM | COD, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY, CREDIT_CARD |
| `payment_status` | ENUM | PENDING, PAID, FAILED, REFUNDED, PARTIAL_REFUND |
| `shipping_method` | ENUM | STANDARD, EXPRESS, SAME_DAY |
| `tracking_number` | VARCHAR(100) | Mã vận đơn |

**Trạng thái đơn hàng (Order Status Flow):**

```
PENDING ──► CONFIRMED ──► PROCESSING ──► SHIPPED ──► DELIVERED ──► COMPLETED
    │           │             │
    │           │             └──────────────► RETURNED
    │           │
    └───────────┴──────────────────────────► CANCELLED
                                                 │
                                                 └──► REFUNDED
```

#### 4.2.2 `order_items` - Chi tiết đơn hàng

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `order_id` | BIGINT | FK → orders.id |
| `product_id` | BIGINT | ID sản phẩm |
| `product_sku` | VARCHAR(50) | Mã SP tại thời điểm đặt (denormalized) |
| `product_name` | VARCHAR(255) | Tên SP tại thời điểm đặt |
| `product_image` | VARCHAR(500) | Ảnh SP |
| `quantity` | INT | Số lượng |
| `unit_price` | DECIMAL(12,2) | Đơn giá tại thời điểm đặt |
| `discount_amount` | DECIMAL(12,2) | Giảm giá |
| `subtotal` | DECIMAL(12,2) | Thành tiền |
| `returned_quantity` | INT | Số lượng đã trả |
| `refunded_amount` | DECIMAL(12,2) | Số tiền đã hoàn |

**Lưu ý:** Thông tin sản phẩm được **denormalized** để giữ lịch sử

#### 4.2.3 `carts` - Giỏ hàng

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `user_id` | BIGINT | ID người dùng (NULL nếu anonymous) |
| `session_id` | VARCHAR(100) | Session ID cho anonymous |
| `status` | ENUM | ACTIVE, MERGED, CONVERTED, ABANDONED |
| `coupon_code` | VARCHAR(50) | Mã giảm giá đang áp dụng |
| `expires_at` | DATETIME | Thời hạn giỏ hàng |

---

## 5. Inventory Database

> **Mục đích:** Quản lý kho hàng, tồn kho, và lịch sử nhập/xuất

### 5.1 Sơ Đồ ERD

```
┌──────────────────────┐
│     warehouses       │
├──────────────────────┤
│ PK id                │
│    code              │
│    name              │
│    address           │
│    city              │
│    phone             │
│    is_active         │
│    is_default        │
└──────────┬───────────┘
           │
           │ 1:N
           ▼
┌──────────────────────┐
│      inventory       │
├──────────────────────┤
│ PK id                │
│    product_id        │
│ FK warehouse_id      │
│    sku               │
│    quantity          │
│    reserved_quantity │
│    reorder_level     │
│    reorder_quantity  │
│    version           │◄──── Optimistic Locking
└──────────┬───────────┘
           │
           │ 1:N
           ▼
┌──────────────────────┐     ┌──────────────────────┐
│ inventory_movements  │     │  stock_reservations  │
├──────────────────────┤     ├──────────────────────┤
│ PK id                │     │ PK id                │
│ FK inventory_id      │     │ FK inventory_id      │
│    product_id        │     │    product_id        │
│ FK warehouse_id      │     │    order_id          │
│    movement_type     │     │    quantity          │
│    quantity          │     │    status            │
│    quantity_before   │     │    expires_at        │
│    quantity_after    │     └──────────────────────┘
│    reference_type    │
│    reference_id      │
│    reason            │
│    notes             │
│    created_by        │
└──────────────────────┘
```

### 5.2 Chi Tiết Bảng

#### 5.2.1 `inventory` - Tồn kho

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `product_id` | BIGINT | ID sản phẩm |
| `warehouse_id` | BIGINT | FK → warehouses.id |
| `sku` | VARCHAR(50) | Mã sản phẩm |
| `quantity` | INT | Số lượng tồn kho vật lý |
| `reserved_quantity` | INT | Số lượng đang giữ cho đơn hàng |
| `reorder_level` | INT | Ngưỡng cảnh báo hết hàng |
| `reorder_quantity` | INT | Số lượng đặt hàng bổ sung |
| `version` | BIGINT | Optimistic locking version |

**Công thức:**
```
available_quantity = quantity - reserved_quantity
```

**Unique Constraint:** (product_id, warehouse_id)

#### 5.2.2 `inventory_movements` - Lịch sử nhập/xuất kho

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `inventory_id` | BIGINT | FK → inventory.id |
| `movement_type` | ENUM | Loại di chuyển kho |
| `quantity` | INT | Số lượng thay đổi |
| `quantity_before` | INT | Số lượng trước |
| `quantity_after` | INT | Số lượng sau |
| `reference_type` | VARCHAR(50) | Loại tham chiếu (ORDER, PURCHASE) |
| `reference_id` | VARCHAR(50) | ID tham chiếu |
| `reason` | VARCHAR(255) | Lý do |
| `created_by` | BIGINT | Người thực hiện |

**Movement Types:**

| Type | Mô Tả |
|------|-------|
| `STOCK_IN` | Nhập hàng từ nhà cung cấp |
| `STOCK_OUT` | Xuất hàng (bán) |
| `RESERVED` | Đặt trước cho đơn hàng pending |
| `RELEASED` | Giải phóng khi hủy đơn |
| `ADJUSTMENT` | Điều chỉnh kiểm kê |
| `TRANSFER_IN` | Chuyển kho vào |
| `TRANSFER_OUT` | Chuyển kho ra |
| `RETURN` | Trả hàng từ khách |
| `DAMAGED` | Hàng hỏng/mất |

#### 5.2.3 `stock_reservations` - Đặt trước hàng

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Khóa chính |
| `inventory_id` | BIGINT | FK → inventory.id |
| `product_id` | BIGINT | ID sản phẩm |
| `order_id` | BIGINT | ID đơn hàng |
| `quantity` | INT | Số lượng đặt trước |
| `status` | ENUM | PENDING, CONFIRMED, RELEASED, CANCELLED |
| `expires_at` | DATETIME | Thời hạn (tự động giải phóng nếu hết) |

---

## 6. Payment Database

> **Mục đích:** Quản lý thanh toán và hoàn tiền

### 6.1 Sơ Đồ ERD

```
┌──────────────────────────────────────────────┐
│                   payments                    │
├──────────────────────────────────────────────┤
│ PK id                                         │
│    payment_code                               │
│    order_id                                   │
│    user_id                                    │
│    amount                                     │
│    currency                                   │
│    payment_method                             │
│    payment_gateway                            │
│    status                                     │
│    gateway_transaction_id                     │
│    gateway_response_code                      │
│    gateway_response_message                   │
│    bank_code                                  │
│    bank_transaction_no                        │
│    paid_at                                    │
│    failed_at                                  │
│    ip_address                                 │
│    user_agent                                 │
│    notes                                      │
└────────────────────────┬─────────────────────┘
                         │
                         │ 1:N
                         ▼
           ┌──────────────────────────────┐
           │           refunds            │
           ├──────────────────────────────┤
           │ PK id                        │
           │    refund_code               │
           │ FK payment_id                │
           │    order_id                  │
           │    amount                    │
           │    reason                    │
           │    status                    │
           │    gateway_refund_id         │
           │    gateway_response_code     │
           │    processed_by              │
           │    processed_at              │
           │    notes                     │
           └──────────────────────────────┘
```

### 6.2 Chi Tiết Bảng

#### 6.2.1 `payments` - Thanh toán

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `payment_code` | VARCHAR(50) | Mã thanh toán (VD: PAY-20260107-123456) |
| `order_id` | BIGINT | ID đơn hàng |
| `user_id` | BIGINT | ID người dùng |
| `amount` | DECIMAL(15,2) | Số tiền thanh toán |
| `payment_method` | ENUM | COD, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY, CREDIT_CARD |
| `payment_gateway` | VARCHAR(50) | Cổng thanh toán sử dụng |
| `status` | ENUM | PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIAL_REFUND |
| `gateway_transaction_id` | VARCHAR(100) | Mã giao dịch từ cổng thanh toán |
| `paid_at` | DATETIME | Thời điểm thanh toán thành công |
| `ip_address` | VARCHAR(45) | IP người thanh toán |

#### 6.2.2 `refunds` - Hoàn tiền

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `refund_code` | VARCHAR(50) | Mã hoàn tiền (VD: REF-20260107-123456) |
| `payment_id` | BIGINT | FK → payments.id |
| `amount` | DECIMAL(15,2) | Số tiền hoàn |
| `reason` | VARCHAR(255) | Lý do hoàn tiền |
| `status` | ENUM | PENDING, PROCESSING, COMPLETED, FAILED, REJECTED |
| `processed_by` | BIGINT | ID admin xử lý |
| `processed_at` | DATETIME | Thời điểm xử lý |

---

## 7. Promotion Database

> **Mục đích:** Quản lý mã giảm giá và flash sale

### 7.1 Sơ Đồ ERD

```
┌──────────────────────────────────────────────┐       ┌──────────────────────┐
│                   coupons                     │       │     flash_sales      │
├──────────────────────────────────────────────┤       ├──────────────────────┤
│ PK id                                         │       │ PK id                │
│    code                                       │       │    name              │
│    name                                       │       │    description       │
│    description                                │       │    banner_url        │
│    discount_type                              │       │    start_time        │
│    discount_value                             │       │    end_time          │
│    minimum_order_amount                       │       │    status            │
│    maximum_discount_amount                    │       │    is_active         │
│    usage_limit                                │       └──────────┬───────────┘
│    usage_limit_per_user                       │                  │
│    usage_count                                │                  │ 1:N
│    start_date                                 │                  ▼
│    end_date                                   │       ┌──────────────────────┐
│    is_active                                  │       │flash_sale_products   │
│    applies_to                                 │       ├──────────────────────┤
│    user_type                                  │       │ PK id                │
└────────────────────────┬─────────────────────┘       │ FK flash_sale_id     │
                         │                             │    product_id        │
                         │ 1:N                         │    flash_sale_price  │
                         ▼                             │    original_price    │
           ┌──────────────────────────────┐            │    quantity_limit    │
           │        coupon_usage           │            │    quantity_sold     │
           ├──────────────────────────────┤            │    per_user_limit    │
           │ PK id                        │            └──────────────────────┘
           │ FK coupon_id                 │
           │    user_id                   │
           │    order_id                  │
           │    discount_amount           │
           │    used_at                   │
           └──────────────────────────────┘

  ┌─────────────────────────┐     ┌─────────────────────────┐
  │    coupon_products      │     │   coupon_categories     │
  │   (Collection Table)    │     │    (Collection Table)   │
  ├─────────────────────────┤     ├─────────────────────────┤
  │ FK coupon_id            │     │ FK coupon_id            │
  │    product_id           │     │    category_id          │
  └─────────────────────────┘     └─────────────────────────┘
```

### 7.2 Chi Tiết Bảng

#### 7.2.1 `coupons` - Mã giảm giá

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `code` | VARCHAR(50) | Mã coupon (VD: SALE50, NEWYEAR2026) |
| `name` | VARCHAR(150) | Tên chương trình |
| `discount_type` | ENUM | PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING |
| `discount_value` | DECIMAL(12,2) | Giá trị giảm (% hoặc số tiền) |
| `minimum_order_amount` | DECIMAL(12,2) | Đơn hàng tối thiểu |
| `maximum_discount_amount` | DECIMAL(12,2) | Giảm tối đa (cho %) |
| `usage_limit` | INT | Tổng lượt sử dụng (NULL = không giới hạn) |
| `usage_limit_per_user` | INT | Giới hạn mỗi user (mặc định: 1) |
| `usage_count` | INT | Số lần đã dùng |
| `start_date` | DATETIME | Bắt đầu hiệu lực |
| `end_date` | DATETIME | Kết thúc hiệu lực |
| `applies_to` | ENUM | ALL, SPECIFIC_PRODUCTS, SPECIFIC_CATEGORIES |
| `user_type` | ENUM | ALL, NEW_USER, VIP, SPECIFIC_USERS |

#### 7.2.2 `flash_sales` - Flash Sale

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `name` | VARCHAR(150) | Tên sự kiện |
| `banner_url` | VARCHAR(500) | Banner quảng cáo |
| `start_time` | DATETIME | Thời điểm bắt đầu |
| `end_time` | DATETIME | Thời điểm kết thúc |
| `status` | ENUM | UPCOMING, ACTIVE, ENDED |

#### 7.2.3 `flash_sale_products` - Sản phẩm Flash Sale

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `flash_sale_id` | BIGINT | FK → flash_sales.id |
| `product_id` | BIGINT | ID sản phẩm |
| `flash_sale_price` | DECIMAL(12,2) | Giá flash sale |
| `original_price` | DECIMAL(12,2) | Giá gốc |
| `quantity_limit` | INT | Số lượng giới hạn |
| `quantity_sold` | INT | Đã bán |
| `per_user_limit` | INT | Giới hạn mỗi user |

---

## 8. Notification Database

> **Mục đích:** Quản lý thông báo, email, SMS

### 8.1 Sơ Đồ ERD

```
┌──────────────────────────────────────┐
│       notification_templates         │
├──────────────────────────────────────┤
│ PK id                                │
│    code                              │  VD: ORDER_CREATED, ORDER_SHIPPED
│    name                              │
│    channel                           │  EMAIL, SMS, PUSH, IN_APP
│    subject                           │  (cho email)
│    body                              │  Template với #{placeholders}
│    is_active                         │
└──────────────────────┬───────────────┘
                       │
                       │ 1:N
                       ▼
┌──────────────────────────────────────┐
│          notifications               │
├──────────────────────────────────────┤
│ PK id                                │
│    user_id                           │
│ FK template_id                       │
│    channel                           │
│    title                             │
│    message                           │  (đã xử lý placeholders)
│    recipient_email                   │
│    recipient_phone                   │
│    status                            │  PENDING, SENT, DELIVERED, FAILED, READ
│    reference_type                    │  ORDER, PAYMENT, PROMOTION
│    reference_id                      │
│    sent_at                           │
│    read_at                           │
│    error_message                     │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│   user_notification_preferences      │
├──────────────────────────────────────┤
│ PK id                                │
│    user_id                           │
│    email_order_updates               │
│    email_promotions                  │
│    email_newsletter                  │
│    sms_order_updates                 │
│    sms_promotions                    │
│    push_enabled                      │
│    push_order_updates                │
│    push_promotions                   │
└──────────────────────────────────────┘
```

---

## 9. Quan Hệ Giữa Các Service

### 9.1 Tham Chiếu Chéo (Cross-Service References)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CROSS-SERVICE DATA FLOW                              │
└─────────────────────────────────────────────────────────────────────────────┘

                              ┌──────────────────┐
                              │   identity_db    │
                              │   users.id       │
                              └────────┬─────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        │                              │                              │
        ▼                              ▼                              ▼
┌───────────────┐            ┌──────────────────┐           ┌─────────────────┐
│  product_db   │            │    order_db      │           │  payment_db     │
│  reviews      │            │    orders        │           │  payments       │
│  wishlists    │            │    carts         │           │  refunds        │
└───────┬───────┘            └────────┬─────────┘           └─────────────────┘
        │                             │
        │                             ▼
        │                    ┌──────────────────┐
        │                    │   inventory_db   │
        │                    │   reservations   │
        │                    └──────────────────┘
        │
        ▼
┌───────────────┐            ┌──────────────────┐
│ promotion_db  │            │ notification_db  │
│ coupon_usage  │            │ notifications    │
└───────────────┘            │ preferences      │
                             └──────────────────┘
```

### 9.2 Bảng Tham Chiếu Chi Tiết

| Source Table | Column | Reference To |
|--------------|--------|--------------|
| `product_db.product_reviews` | `user_id` | `identity_db.users.id` |
| `product_db.wishlists` | `user_id` | `identity_db.users.id` |
| `order_db.carts` | `user_id` | `identity_db.users.id` |
| `order_db.orders` | `user_id` | `identity_db.users.id` |
| `order_db.cart_items` | `product_id` | `product_db.products.id` |
| `order_db.order_items` | `product_id` | `product_db.products.id` |
| `inventory_db.inventory` | `product_id` | `product_db.products.id` |
| `inventory_db.stock_reservations` | `order_id` | `order_db.orders.id` |
| `payment_db.payments` | `order_id` | `order_db.orders.id` |
| `payment_db.payments` | `user_id` | `identity_db.users.id` |
| `promotion_db.coupon_usage` | `order_id` | `order_db.orders.id` |
| `promotion_db.coupon_usage` | `user_id` | `identity_db.users.id` |
| `notification_db.notifications` | `user_id` | `identity_db.users.id` |

---

## 10. Quy Ước Chung

### 10.1 Naming Conventions

| Loại | Quy Ước | Ví Dụ |
|------|---------|-------|
| Table | snake_case, số nhiều | `users`, `order_items` |
| Column | snake_case | `created_at`, `user_id` |
| Primary Key | `id` | - |
| Foreign Key | `<table>_id` | `user_id`, `order_id` |
| Index | `idx_<table>_<column>` | `idx_users_email` |
| Unique Constraint | `uk_<table>_<column>` | `uk_users_email` |

### 10.2 Data Types

| Mục Đích | Kiểu Dữ Liệu | Lưu Ý |
|----------|--------------|-------|
| ID | BIGINT AUTO_INCREMENT | - |
| Tiền tệ | DECIMAL(12,2) hoặc (15,2) | Không dùng FLOAT |
| Phần trăm | DECIMAL(5,2) | 0.00 - 100.00 |
| Thời gian | DATETIME | UTC timezone |
| Boolean | BOOLEAN | - |
| Text ngắn | VARCHAR(n) | n phù hợp |
| Text dài | TEXT | - |
| URL | VARCHAR(500) | - |
| Enum | ENUM hoặc VARCHAR | - |

### 10.3 Audit Fields

Tất cả các bảng đều có:

```sql
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
```

### 10.4 Soft Delete

Sử dụng cờ trạng thái thay vì xóa cứng:
- `is_active` cho entities có thể vô hiệu hóa
- `status` cho entities có nhiều trạng thái

### 10.5 Optimistic Locking

Sử dụng `version` column cho các bảng có nguy cơ race condition:
- `inventory` - tránh overselling

---

## 📝 Ghi Chú

1. **Character Set:** UTF8MB4 cho tất cả tables
2. **Collation:** utf8mb4_unicode_ci
3. **Engine:** InnoDB cho transaction support
4. **Indexes:** Tạo index cho các cột thường xuyên query

---

*Tài liệu được tạo tự động từ Entity definitions - BookStore Microservices Project*
