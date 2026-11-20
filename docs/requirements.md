# TÀI LIỆU YÊU CẦU HỆ THỐNG: TICKCORE PHOTO

Website hỗ trợ đặt lịch chụp ảnh giữa khách hàng và nhiếp ảnh gia.

| Mục | Nội dung |
|---|---|
| Tên hệ thống | Tickcore Photo |
| Kiến trúc | Monolith, REST API (Spring Boot 4.0.10, Java 21, PostgreSQL) |
| Phiên bản tài liệu | 1.0 |
| Ngày tạo | 20/11/2026 |

---

## 1. Giới thiệu

### 1.1 Mục tiêu
Xây dựng nền tảng web cho phép khách hàng tìm nhiếp ảnh gia, xem gói chụp, chọn khung giờ còn trống và đặt lịch trực tuyến. Nhiếp ảnh gia quản lý lịch làm việc và duyệt lịch đặt. Quản trị viên giám sát hệ thống và xem thống kê.

### 1.2 Phạm vi
**Trong phạm vi:**
- Quản lý tài khoản và phân quyền (JWT).
- Quản lý hồ sơ nhiếp ảnh gia, gói chụp, lịch rảnh, ngày nghỉ, ảnh mẫu.
- Đặt lịch, hủy lịch, duyệt lịch, theo dõi trạng thái.
- Chống trùng lịch.
- Gửi email xác nhận và nhắc lịch.
- Đánh giá sau buổi chụp.
- Thống kê cho quản trị viên.

**Ngoài phạm vi (hướng phát triển):**
- Thanh toán trực tuyến thật (chỉ ghi nhận trạng thái cọc).
- Ứng dụng di động, chat realtime, thông báo đẩy.
- Gợi ý bằng AI, đa ngôn ngữ, đa chi nhánh.

### 1.3 Thuật ngữ

| Thuật ngữ | Giải thích |
|---|---|
| Khách hàng (Customer) | Người đặt lịch chụp |
| Nhiếp ảnh gia (Photographer) | Người cung cấp dịch vụ chụp |
| Quản trị viên (Admin) | Người quản lý hệ thống |
| Gói chụp (Service Package) | Dịch vụ có giá và thời lượng cố định |
| Khung giờ trống (Slot) | Khoảng thời gian nhiếp ảnh gia còn rảnh |
| Booking | Một lịch đặt chụp |

---

## 2. Các tác nhân (Actor)

| Actor | Mô tả | Quyền chính |
|---|---|---|
| Khách vãng lai (Guest) | Chưa đăng nhập | Xem danh sách nhiếp ảnh gia, gói chụp, ảnh mẫu, đánh giá; đăng ký; đăng nhập |
| Khách hàng | Đã đăng nhập, role CUSTOMER | Đặt, hủy, xem lịch; đánh giá; cập nhật hồ sơ |
| Nhiếp ảnh gia | Role PHOTOGRAPHER | Quản lý hồ sơ, gói chụp, lịch rảnh, ngày nghỉ, ảnh mẫu; duyệt hoặc từ chối lịch; đánh dấu hoàn thành |
| Quản trị viên | Role ADMIN | Quản lý người dùng, xem thống kê, xử lý nội dung vi phạm |
| Hệ thống (Scheduler) | Tác nhân tự động | Gửi email nhắc lịch, tự hủy lịch quá hạn chưa duyệt |

---

## 3. Yêu cầu chức năng

Mức ưu tiên: **H** (cao, bắt buộc), **M** (trung bình), **L** (thấp, làm nếu kịp).

### 3.1 Xác thực và tài khoản

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-01 | Người dùng đăng ký tài khoản bằng email, mật khẩu, họ tên, số điện thoại | H |
| FR-02 | Email là duy nhất; mật khẩu được băm (BCrypt) | H |
| FR-03 | Đăng nhập bằng email và mật khẩu, hệ thống trả về JWT (access token) | H |
| FR-04 | Hệ thống phân quyền theo role: CUSTOMER, PHOTOGRAPHER, ADMIN | H |
| FR-05 | Người dùng xem và cập nhật hồ sơ cá nhân | H |
| FR-06 | Đổi mật khẩu | M |
| FR-07 | Quên mật khẩu, đặt lại qua email | M |
| FR-08 | Đăng ký làm nhiếp ảnh gia, tạo hồ sơ chờ Admin duyệt | M |

### 3.2 Nhiếp ảnh gia và gói chụp

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-10 | Nhiếp ảnh gia tạo và sửa hồ sơ: giới thiệu, chuyên môn, thành phố | H |
| FR-11 | Nhiếp ảnh gia tạo, sửa, ẩn gói chụp (tên, mô tả, giá, thời lượng) | H |
| FR-12 | Nhiếp ảnh gia tải lên và xóa ảnh mẫu (portfolio) | M |
| FR-13 | Khách xem danh sách nhiếp ảnh gia, có phân trang | H |
| FR-14 | Tìm kiếm và lọc theo chuyên môn, thành phố, khoảng giá, điểm đánh giá | H |
| FR-15 | Xem chi tiết nhiếp ảnh gia: hồ sơ, gói chụp, ảnh mẫu, đánh giá | H |

### 3.3 Lịch làm việc

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-20 | Nhiếp ảnh gia khai báo lịch rảnh định kỳ theo thứ trong tuần (giờ bắt đầu, kết thúc) | H |
| FR-21 | Nhiếp ảnh gia khai báo ngày nghỉ hoặc khoảng thời gian bận (TimeOff) | H |
| FR-22 | Hệ thống tính và trả về các khung giờ trống theo ngày, theo gói chụp đã chọn | H |
| FR-23 | Gợi ý khung giờ trống gần nhất khi khung giờ khách chọn đã bị đặt | M |

### 3.4 Đặt lịch

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-30 | Khách chọn nhiếp ảnh gia, gói chụp, ngày, khung giờ, địa điểm, ghi chú để tạo booking | H |
| FR-31 | Hệ thống kiểm tra khung giờ hợp lệ: nằm trong lịch rảnh, không trùng TimeOff, không ở quá khứ | H |
| FR-32 | Hệ thống **chống trùng lịch**: một nhiếp ảnh gia không có hai booking chồng giờ (PENDING hoặc CONFIRMED) | H |
| FR-33 | Booking mới có trạng thái PENDING, chờ nhiếp ảnh gia duyệt | H |
| FR-34 | Nhiếp ảnh gia xác nhận (CONFIRMED) hoặc từ chối (REJECTED, kèm lý do) | H |
| FR-35 | Khách hủy booking trước thời hạn cho phép (CANCELLED) | H |
| FR-36 | Nhiếp ảnh gia đánh dấu COMPLETED sau buổi chụp, hoặc NO_SHOW nếu khách vắng | H |
| FR-37 | Khách và nhiếp ảnh gia xem danh sách booking của mình, lọc theo trạng thái, ngày | H |
| FR-38 | Xem chi tiết booking và lịch sử đổi trạng thái | M |
| FR-39 | Khách đổi lịch (chọn khung giờ khác), áp dụng lại toàn bộ kiểm tra trùng lịch | L |
| FR-40 | Ghi nhận trạng thái đặt cọc (chưa cọc, đã cọc), không tích hợp cổng thanh toán | L |

### 3.5 Thông báo

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-50 | Gửi email cho khách khi tạo booking thành công | H |
| FR-51 | Gửi email cho nhiếp ảnh gia khi có booking mới | H |
| FR-52 | Gửi email khi booking được xác nhận, bị từ chối hoặc bị hủy | H |
| FR-53 | Gửi email nhắc lịch trước buổi chụp 24 giờ | M |
| FR-54 | Gửi email chào mừng, xác thực email khi đăng ký | L |

### 3.6 Đánh giá

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-60 | Khách đánh giá (1 đến 5 sao, nhận xét) sau khi booking COMPLETED | H |
| FR-61 | Mỗi booking chỉ được đánh giá một lần | H |
| FR-62 | Hệ thống tự cập nhật điểm trung bình của nhiếp ảnh gia | H |
| FR-63 | Admin ẩn hoặc xóa đánh giá vi phạm | M |

### 3.7 Quản trị và thống kê

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-70 | Admin xem, tìm kiếm, khóa hoặc mở khóa tài khoản | H |
| FR-71 | Admin duyệt hồ sơ nhiếp ảnh gia | M |
| FR-72 | Admin xem thống kê: số booking theo tháng, theo trạng thái | H |
| FR-73 | Admin xem thống kê doanh thu theo tháng, top nhiếp ảnh gia, top gói chụp | M |
| FR-74 | Xuất báo cáo thống kê ra CSV hoặc Excel | L |

---

## 4. Quy tắc nghiệp vụ

| Mã | Quy tắc |
|---|---|
| BR-01 | Mỗi nhiếp ảnh gia chỉ có tối đa một booking đang hoạt động (PENDING, CONFIRMED) tại một thời điểm. Hai khoảng thời gian chồng lấn nếu `start1 < end2` và `start2 < end1`. |
| BR-02 | Giờ kết thúc của booking bằng giờ bắt đầu cộng thời lượng của gói chụp. |
| BR-03 | Không được đặt lịch trong quá khứ. Nên đặt trước tối thiểu 12 giờ (cấu hình được). |
| BR-04 | Booking PENDING quá 24 giờ chưa được duyệt sẽ tự động chuyển CANCELLED và thông báo cho khách. |
| BR-05 | Khách chỉ hủy được khi booking còn PENDING hoặc CONFIRMED và còn cách giờ chụp tối thiểu 24 giờ (cấu hình được). |
| BR-06 | Chỉ booking COMPLETED mới được đánh giá, và chỉ khách đã đặt booking đó mới được đánh giá. |
| BR-07 | Giá booking được chốt tại thời điểm đặt (lưu `totalPrice`), thay đổi giá gói chụp sau đó không ảnh hưởng booking cũ. |
| BR-08 | Gói chụp đã có booking không được xóa cứng, chỉ được ẩn (`active = false`). |
| BR-09 | Nhiếp ảnh gia không được đặt lịch cho chính mình. |
| BR-10 | Tài khoản bị khóa không đăng nhập được, booking hiện có vẫn được giữ lại. |
| BR-11 | Mọi thời gian lưu dạng UTC trong CSDL, hiển thị theo múi giờ `Asia/Ho_Chi_Minh`. |

### Sơ đồ trạng thái Booking

```
                +--------------+
   tạo mới ---> |   PENDING    | ---(nhiếp ảnh gia từ chối)---> REJECTED
                +--------------+
                   |        |
        (xác nhận) |        +---(khách hủy / quá hạn duyệt)---> CANCELLED
                   v
                +--------------+
                |  CONFIRMED   | ---(khách hủy)---> CANCELLED
                +--------------+
                   |        |
        (hoàn thành)        +---(khách vắng)---> NO_SHOW
                   v
                COMPLETED
```

---

## 5. Yêu cầu phi chức năng

| Mã | Nhóm | Yêu cầu |
|---|---|---|
| NFR-01 | Bảo mật | Mật khẩu băm BCrypt; API bảo vệ bằng JWT; kiểm tra quyền ở từng endpoint |
| NFR-02 | Bảo mật | Chống SQL Injection (dùng JPA, tham số hóa), kiểm tra dữ liệu đầu vào bằng Bean Validation |
| NFR-03 | Bảo mật | Cấu hình CORS đúng; không trả mật khẩu hoặc dữ liệu nhạy cảm trong response |
| NFR-04 | Hiệu năng | API danh sách và tìm kiếm phản hồi dưới 2 giây với 10.000 bản ghi; có phân trang |
| NFR-05 | Toàn vẹn dữ liệu | Chống trùng lịch ở cả tầng service và tầng CSDL (ràng buộc, `@Version`) khi có request đồng thời |
| NFR-06 | Độ tin cậy | Lỗi gửi email không làm hỏng việc tạo booking (gửi bất đồng bộ hoặc ghi log và thử lại) |
| NFR-07 | Khả dụng | Chạy được bằng Docker Compose (ứng dụng và PostgreSQL) bằng một lệnh |
| NFR-08 | Bảo trì | Mã nguồn chia tầng rõ ràng; có migration bằng Flyway; `ddl-auto: validate` |
| NFR-09 | Kiểm thử | Unit test cho service đặt lịch; integration test cho luồng đặt lịch và chống trùng |
| NFR-10 | Tài liệu | API có tài liệu Swagger/OpenAPI |
| NFR-11 | Khả năng dùng | Giao diện hiển thị tốt trên máy tính và điện thoại (responsive) |
| NFR-12 | Lỗi | Trả về mã lỗi HTTP và thông báo thống nhất (định dạng `ApiResponse`) |

---

## 6. Danh sách Use Case

| Mã | Use case | Actor chính |
|---|---|---|
| UC-01 | Đăng ký tài khoản | Guest |
| UC-02 | Đăng nhập | Guest |
| UC-03 | Quản lý hồ sơ cá nhân | Khách hàng, Nhiếp ảnh gia |
| UC-04 | Tìm kiếm và lọc nhiếp ảnh gia | Guest, Khách hàng |
| UC-05 | Xem chi tiết nhiếp ảnh gia | Guest, Khách hàng |
| UC-06 | Quản lý gói chụp | Nhiếp ảnh gia |
| UC-07 | Quản lý lịch rảnh | Nhiếp ảnh gia |
| UC-08 | Quản lý ngày nghỉ (TimeOff) | Nhiếp ảnh gia |
| UC-09 | Quản lý ảnh mẫu | Nhiếp ảnh gia |
| UC-10 | Xem khung giờ trống | Khách hàng |
| UC-11 | Đặt lịch chụp | Khách hàng |
| UC-12 | Hủy lịch | Khách hàng |
| UC-13 | Xem danh sách và chi tiết lịch đặt | Khách hàng, Nhiếp ảnh gia |
| UC-14 | Duyệt hoặc từ chối lịch | Nhiếp ảnh gia |
| UC-15 | Hoàn thành buổi chụp / đánh dấu vắng mặt | Nhiếp ảnh gia |
| UC-16 | Đánh giá nhiếp ảnh gia | Khách hàng |
| UC-17 | Quản lý người dùng | Admin |
| UC-18 | Duyệt hồ sơ nhiếp ảnh gia | Admin |
| UC-19 | Xem thống kê | Admin |
| UC-20 | Kiểm duyệt đánh giá | Admin |
| UC-21 | Gửi email nhắc lịch và tự hủy lịch quá hạn | Hệ thống |

---

## 7. Đặc tả Use Case chi tiết

### UC-01: Đăng ký tài khoản

| Mục | Nội dung |
|---|---|
| Actor | Guest |
| Mô tả | Người dùng tạo tài khoản mới |
| Tiền điều kiện | Chưa đăng nhập |
| Hậu điều kiện | Tài khoản được tạo với role CUSTOMER |

**Luồng chính**
1. Guest mở form đăng ký, nhập email, mật khẩu, họ tên, số điện thoại.
2. Hệ thống kiểm tra định dạng dữ liệu.
3. Hệ thống kiểm tra email chưa tồn tại.
4. Hệ thống băm mật khẩu, lưu tài khoản.
5. Hệ thống thông báo đăng ký thành công và chuyển đến trang đăng nhập.

**Luồng thay thế / ngoại lệ**
- 2a. Dữ liệu không hợp lệ: hiển thị lỗi cụ thể từng trường.
- 3a. Email đã tồn tại: thông báo "Email đã được sử dụng".

---

### UC-02: Đăng nhập

| Mục | Nội dung |
|---|---|
| Actor | Guest |
| Tiền điều kiện | Đã có tài khoản |
| Hậu điều kiện | Người dùng nhận JWT, được chuyển đến trang theo role |

**Luồng chính**
1. Người dùng nhập email và mật khẩu.
2. Hệ thống xác thực thông tin.
3. Hệ thống tạo và trả JWT cùng thông tin người dùng.

**Ngoại lệ**
- 2a. Sai email hoặc mật khẩu: thông báo "Thông tin đăng nhập không đúng".
- 2b. Tài khoản bị khóa: thông báo tài khoản đã bị khóa.

---

### UC-07: Quản lý lịch rảnh

| Mục | Nội dung |
|---|---|
| Actor | Nhiếp ảnh gia |
| Tiền điều kiện | Đã đăng nhập với role PHOTOGRAPHER, có hồ sơ được duyệt |
| Hậu điều kiện | Lịch rảnh được cập nhật, ảnh hưởng đến khung giờ trống |

**Luồng chính**
1. Nhiếp ảnh gia mở trang quản lý lịch.
2. Chọn thứ trong tuần, nhập giờ bắt đầu và giờ kết thúc.
3. Hệ thống kiểm tra giờ bắt đầu nhỏ hơn giờ kết thúc và không chồng với khoảng đã có cùng thứ.
4. Hệ thống lưu lịch rảnh.

**Ngoại lệ**
- 3a. Khoảng giờ không hợp lệ hoặc chồng lấn: hiển thị lỗi.
- 4a. Xóa khoảng rảnh đang có booking trong tương lai: hệ thống cảnh báo và từ chối.

---

### UC-10: Xem khung giờ trống

| Mục | Nội dung |
|---|---|
| Actor | Khách hàng |
| Tiền điều kiện | Đã chọn nhiếp ảnh gia và gói chụp |
| Hậu điều kiện | Hiển thị danh sách khung giờ có thể đặt |

**Luồng chính**
1. Khách chọn ngày muốn chụp.
2. Hệ thống lấy lịch rảnh của nhiếp ảnh gia theo thứ của ngày đó.
3. Hệ thống loại bỏ các khoảng trùng TimeOff và booking PENDING, CONFIRMED.
4. Hệ thống chia các khoảng còn lại thành khung giờ theo thời lượng gói chụp.
5. Hệ thống trả danh sách khung giờ trống.

**Ngoại lệ**
- 5a. Không còn khung giờ trống: hiển thị thông báo và gợi ý ngày gần nhất còn trống (FR-23).

---

### UC-11: Đặt lịch chụp

| Mục | Nội dung |
|---|---|
| Actor | Khách hàng |
| Tiền điều kiện | Đã đăng nhập với role CUSTOMER; đã chọn khung giờ trống |
| Hậu điều kiện | Booking mới ở trạng thái PENDING; email thông báo được gửi |

**Luồng chính**
1. Khách chọn gói chụp, ngày, khung giờ.
2. Khách nhập địa điểm chụp và ghi chú.
3. Khách xác nhận đặt lịch.
4. Hệ thống kiểm tra khung giờ hợp lệ (BR-02, BR-03).
5. Hệ thống kiểm tra khung giờ nằm trong lịch rảnh và không trùng TimeOff.
6. Hệ thống kiểm tra không trùng booking khác (BR-01).
7. Hệ thống lưu booking với trạng thái PENDING và giá tại thời điểm đặt (BR-07).
8. Hệ thống gửi email cho khách và nhiếp ảnh gia.
9. Hệ thống hiển thị mã booking và trạng thái.

**Luồng thay thế / ngoại lệ**
- 4a. Khung giờ ở quá khứ hoặc quá sát giờ: báo lỗi, yêu cầu chọn lại.
- 5a. Ngoài lịch rảnh hoặc trùng TimeOff: báo lỗi.
- 6a. Trùng lịch (kể cả khi hai người đặt đồng thời): báo "Khung giờ vừa được người khác đặt", gợi ý khung giờ gần nhất.
- 8a. Gửi email thất bại: ghi log, không hủy booking (NFR-06).

---

### UC-12: Hủy lịch

| Mục | Nội dung |
|---|---|
| Actor | Khách hàng |
| Tiền điều kiện | Booking thuộc về khách, trạng thái PENDING hoặc CONFIRMED |
| Hậu điều kiện | Booking chuyển CANCELLED, khung giờ được giải phóng |

**Luồng chính**
1. Khách mở chi tiết booking, chọn "Hủy lịch" và nhập lý do.
2. Hệ thống kiểm tra thời hạn hủy (BR-05).
3. Hệ thống đổi trạng thái sang CANCELLED, lưu lý do.
4. Hệ thống gửi email cho nhiếp ảnh gia và khách.

**Ngoại lệ**
- 2a. Quá thời hạn hủy: từ chối và hướng dẫn liên hệ nhiếp ảnh gia.

---

### UC-14: Duyệt hoặc từ chối lịch

| Mục | Nội dung |
|---|---|
| Actor | Nhiếp ảnh gia |
| Tiền điều kiện | Booking thuộc nhiếp ảnh gia, trạng thái PENDING |
| Hậu điều kiện | Booking chuyển CONFIRMED hoặc REJECTED |

**Luồng chính**
1. Nhiếp ảnh gia xem danh sách booking chờ duyệt.
2. Chọn một booking, xem chi tiết.
3. Chọn "Xác nhận".
4. Hệ thống đổi trạng thái sang CONFIRMED và gửi email cho khách.

**Luồng thay thế**
- 3a. Chọn "Từ chối", nhập lý do: hệ thống đổi sang REJECTED, gửi email kèm lý do, giải phóng khung giờ.

**Ngoại lệ**
- 4a. Booking đã bị khách hủy hoặc quá hạn duyệt: thông báo trạng thái đã thay đổi, không cho thao tác.
- 4b. Hai thao tác đồng thời trên cùng booking: hệ thống dùng `@Version`, thao tác sau báo lỗi và yêu cầu tải lại.

---

### UC-15: Hoàn thành buổi chụp

| Mục | Nội dung |
|---|---|
| Actor | Nhiếp ảnh gia |
| Tiền điều kiện | Booking CONFIRMED và đã qua giờ kết thúc |
| Hậu điều kiện | Booking COMPLETED hoặc NO_SHOW |

**Luồng chính**
1. Nhiếp ảnh gia mở booking đã diễn ra.
2. Chọn "Hoàn thành".
3. Hệ thống đổi trạng thái COMPLETED, mở quyền đánh giá cho khách.

**Luồng thay thế**
- 2a. Chọn "Khách vắng mặt": hệ thống đổi sang NO_SHOW.

---

### UC-16: Đánh giá nhiếp ảnh gia

| Mục | Nội dung |
|---|---|
| Actor | Khách hàng |
| Tiền điều kiện | Booking của khách ở trạng thái COMPLETED, chưa được đánh giá |
| Hậu điều kiện | Đánh giá được lưu, điểm trung bình nhiếp ảnh gia được cập nhật |

**Luồng chính**
1. Khách mở booking đã hoàn thành, chọn "Đánh giá".
2. Khách chọn số sao (1 đến 5) và nhập nhận xét.
3. Hệ thống kiểm tra điều kiện (BR-06).
4. Hệ thống lưu đánh giá và tính lại điểm trung bình.

**Ngoại lệ**
- 3a. Đã đánh giá rồi hoặc booking chưa COMPLETED: từ chối.

---

### UC-19: Xem thống kê

| Mục | Nội dung |
|---|---|
| Actor | Admin |
| Tiền điều kiện | Đăng nhập với role ADMIN |
| Hậu điều kiện | Hiển thị số liệu thống kê |

**Luồng chính**
1. Admin mở trang thống kê, chọn khoảng thời gian.
2. Hệ thống tổng hợp số booking theo trạng thái, theo tháng, doanh thu từ booking COMPLETED.
3. Hệ thống hiển thị biểu đồ và bảng, kèm top nhiếp ảnh gia và gói chụp.

---

### UC-21: Gửi email nhắc lịch và tự hủy lịch quá hạn

| Mục | Nội dung |
|---|---|
| Actor | Hệ thống (Scheduler) |
| Kích hoạt | Chạy định kỳ (ví dụ mỗi 15 phút) |

**Luồng chính**
1. Hệ thống tìm booking CONFIRMED bắt đầu trong khoảng 24 giờ tới, chưa gửi nhắc: gửi email nhắc cho khách và nhiếp ảnh gia, đánh dấu đã gửi.
2. Hệ thống tìm booking PENDING quá 24 giờ chưa duyệt: chuyển CANCELLED, gửi email thông báo.

---

## 8. Ma trận phân quyền

| Chức năng | Guest | Customer | Photographer | Admin |
|---|:---:|:---:|:---:|:---:|
| Đăng ký, đăng nhập | X | | | |
| Xem nhiếp ảnh gia, gói chụp, ảnh mẫu | X | X | X | X |
| Xem khung giờ trống | X | X | X | X |
| Đặt lịch | | X | | |
| Hủy lịch của mình | | X | | |
| Đánh giá | | X | | |
| Quản lý gói chụp, lịch rảnh, ảnh mẫu | | | X | |
| Duyệt, từ chối, hoàn thành lịch | | | X | |
| Quản lý người dùng, duyệt hồ sơ | | | | X |
| Xem thống kê | | | | X |
| Kiểm duyệt đánh giá | | | | X |

---

## 9. Danh sách thực thể chính

| Entity | Mô tả |
|---|---|
| User | Tài khoản (email, passwordHash, fullName, phone, role, enabled) |
| PhotographerProfile | Hồ sơ nhiếp ảnh gia (bio, specialties, city, avgRating, approved) |
| ServicePackage | Gói chụp (name, description, price, durationMinutes, active) |
| Availability | Lịch rảnh định kỳ (dayOfWeek, startTime, endTime) |
| TimeOff | Ngày nghỉ (startAt, endAt, reason) |
| Booking | Lịch đặt (customer, photographer, servicePackage, startAt, endAt, status, location, note, totalPrice, version) |
| Review | Đánh giá (booking, rating, comment, visible) |
| PortfolioImage | Ảnh mẫu (photographer, imageUrl, caption) |

**Quan hệ chính**
- User 1-1 PhotographerProfile (với role PHOTOGRAPHER)
- PhotographerProfile 1-N ServicePackage, Availability, TimeOff, PortfolioImage
- User (khách) 1-N Booking; PhotographerProfile 1-N Booking
- ServicePackage 1-N Booking
- Booking 1-1 Review

---

## 10. Giả định và ràng buộc

- Một booking gắn với đúng một nhiếp ảnh gia và một gói chụp.
- Lịch rảnh đặt theo giờ Việt Nam, không hỗ trợ nhiều múi giờ.
- Thanh toán ngoài hệ thống; hệ thống chỉ ghi nhận trạng thái cọc (nếu làm FR-40).
- Ảnh mẫu lưu bằng dịch vụ ngoài (Cloudinary) hoặc thư mục cục bộ.
- Công nghệ: Spring Boot 4.0.10, Java 21, PostgreSQL, Flyway, Spring Security (JWT), Maven.

## 11. Hướng phát triển

- Thanh toán trực tuyến (VNPay, MoMo), hoàn tiền khi hủy.
- Ứng dụng di động, thông báo đẩy.
- Gợi ý nhiếp ảnh gia bằng AI dựa trên phong cách và đánh giá.
- Đồng bộ lịch với Google Calendar.
- Chat giữa khách và nhiếp ảnh gia.
