# 📱 Student Attendance System (Android App)

A complete **Student Attendance Management System** built with **Android (Kotlin)** and **Firebase Firestore**. This app allows teachers to manage students, generate QR codes, and mark attendance efficiently, while students can view their attendance and scan QR codes.

## ✨ Features

### 👨‍🏫 For Teachers / Admin
* **Secure Login/Register:** Role-based authentication (Admin/Teacher/Student).
* **Manage Students:** Add new students with details (Name, Reg No, Department, Year).
    * *Includes validation to prevent duplicate students.*
* **Generate Class QR:** Create unique QR codes for specific subjects and dates.
* **Manual Attendance:** Mark attendance manually for students who cannot scan.
* **View Reports:** Check attendance statistics and view detailed lists.
* **Export to PDF:** Generate and save attendance reports as PDF files.

### 👨‍🎓 For Students
* **Student Dashboard:** View personal attendance records.
* **Scan QR:** Mark attendance instantly by scanning the teacher's QR code.
* **My QR Code:** View their own digital Student ID (QR code).

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Database:** Google Firebase Firestore (Cloud Database)
* **Authentication:** SQLite (Local Login) & Firebase
* **QR Code:** ZXing Library & CodeScanner
* **PDF Generation:** Native Android PDF Document API

## 🚀 How to Run

1.  Clone this repository.
2.  Open the project in **Android Studio**.
3.  Connect the project to your **Firebase Console**:
    * Add your `google-services.json` file to the `app/` folder.
    * Enable **Firestore Database** in test mode.
4.  Sync Gradle and Run on an Emulator or Real Device.


## 📸 Screenshots

| Login Screen | Teacher Dashboard | Student Dashboard | Add Student | Mark Attendance |
|:---:|:---:|:---:|:---:|:---:|
| ![Login](https://github.com/user-attachments/assets/f3172723-3611-4b87-88d9-c6125a722f71) | ![TeacherDash](https://github.com/user-attachments/assets/50da2f73-2ff5-4c9c-ad33-ec6f1a76c24a) | ![StudentDash](https://github.com/user-attachments/assets/ed5a89c4-99bd-4d78-9427-e83ffc49f9d8) | ![AddStudent](https://github.com/user-attachments/assets/07061873-67c1-48b3-961f-6954c63df80e) | ![MarkAttendance](https://github.com/user-attachments/assets/ea39edf1-5b4c-42ad-8dcb-6fbb673ace14) |

## 🤝 Contributing

Contributions are welcome! Feel free to fork this repository and submit pull requests.

## 📞 Contact

Developed by: **[Pabasara Ravihara]**
* Email: [pabasararavihara2001@gmail.com]
* GitHub: [https://github.com/PabasaraRavihara/Android-Studio-Project.git]

---
*This project was developed for educational purposes.*
