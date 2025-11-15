# SLANG WORD DICTIONARY (TỪ ĐIỂN TỪ LÓNG)

## I. THÔNG TIN CHUNG

* **Tên Môn học:** CSC13002 LẬP TRÌNH ỨNG DỤNG JAVA
* **Đồ án:** Đồ án #1 – SLANG WORD
* **Công nghệ:** JavaFX, Java IO, OOP, Collections.

---

## II. YÊU CẦU KỸ THUẬT VÀ CÀI ĐẶT

### 1. Yêu cầu Tiên quyết (Pre-requisites)

Để chạy ứng dụng JavaFX này, hệ thống của bạn cần có:

* **JDK (Java Development Kit):** Phiên bản **17 trở lên**.
* **JavaFX SDK:** Cần có JavaFX SDK tương thích với phiên bản JDK của bạn (vì ứng dụng được Build dưới dạng JAR độc lập).

### 2. Chuẩn bị Cấu trúc Thư mục

Ứng dụng yêu cầu thư mục **`data`** phải nằm cùng cấp với file `.jar` để có thể truy cập các file dữ liệu (`slang.txt` và `slang.dat`).

**[Thư mục Chạy Ứng Dụng]**
* **SlangWord.jar**  
* **data**          
    * ***slang.txt***  
    * ***slang.dat***  (File này sẽ được tạo ra sau lần chạy đầu tiên)
***
### III. HƯỚNG DẪN CHẠY ỨNG DỤNG

Bạn cần sử dụng **Command Line** (Terminal/CMD) để cung cấp thư viện JavaFX khi chạy file `.jar`.

#### Bước 1: Mở Command Prompt/Terminal

Mở Command Prompt (CMD) hoặc Terminal và điều hướng đến thư mục chứa file `SlangWord.jar` của bạn.

#### Bước 2: Thực thi Lệnh

Thực hiện lệnh sau. **Bạn cần thay thế phần `<ĐƯỜNG_DẪN_TỚI_JAVA_FX_SDK_lib>`** bằng đường dẫn chính xác đến thư mục `lib` của JavaFX SDK trên máy tính của bạn:

```bash
java --module-path <ĐƯỜNG_DẪN_TỚI_JAVA_FX_SDK_lib> --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar SlangWord.jar
```
Ví dụ:
```bash
java --module-path C:\javafx-sdk-21\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar SlangWord.jar
```