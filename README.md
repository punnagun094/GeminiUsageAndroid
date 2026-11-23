<div align="center">

# ✨ Gemini Usage for Android
**ตรวจสอบโควตาการใช้งาน Gemini AI ภายในคลิกเดียว พร้อม Widget หน้าจอโฮมและรองรับจอด้านหลัง (Xiaomi Rear Screen)**  
*Check your Gemini AI usage quota in one click, featuring a Home Screen Widget and Xiaomi Rear Screen support.*

![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Design](https://img.shields.io/badge/UI-Liquid%20Glass%20%C3%97%20Material%203-6750A4?style=for-the-badge)

<br/>

<table>
  <tr>
    <td align="center"><b>Main App</b></td>
    <td align="center"><b>Home Widget</b></td>
    <td align="center"><b>Xiaomi Rear Screen</b></td>
  </tr>
  <tr>
    <td align="center"><img width="210" alt="Main App" src="https://github.com/user-attachments/assets/1d6988e2-270b-4eac-8f45-ddcd38fa019d" /></td>
    <td align="center"><img width="210" alt="Home Screen Widget" src="https://github.com/user-attachments/assets/5850729e-91da-469f-8f8b-446fc2b22fde" /></td>
    <td align="center"><img width="340" alt="Xiaomi Rear Display" src="https://github.com/user-attachments/assets/dd3e3833-d376-4f3e-862a-e9e30160edc4" /></td>
  </tr>
</table>

</div>

---

## 🌟 ฟีเจอร์เด่น

- **📊 เช็กโควตาครบทั้ง 2 รอบ:** แสดงเปอร์เซ็นต์การใช้งานและเวลารีเซ็ตถัดไปทั้งรอบ **5 ชั่วโมง (5-Hour Limit)** และ **รายสัปดาห์ (Weekly Limit)**
- **🖼️ Home Screen Widget ดีไซน์ทูโทน:** วาง Widget ไว้บนหน้าจอโฮมเพื่อดูหลอดสถานะโควตาได้ทันทีโดยไม่ต้องเปิดแอป
- **📱 รองรับจอด้านหลัง (Xiaomi 17 Pro Max, 18 Pro Max / Rear Display):** ปรับระยะขอบและการแสดงผลอัตโนมัติเมื่อเปิดใช้งานบนหน้าจอด้านหลังผ่านแอป [Magic Rear](https://gitlab.com/896kb/ai-usage-companion/-/releases) (พัฒนาโดย **88896**)
- **🔄 ซิงค์ข้อมูลเบื้องหลังอัตโนมัติ:** อัปเดตตัวเลขตามรอบเวลาที่ตั้งไว้ (ทุก ๆ 2 ชั่วโมง) พร้อมระบบป้องกันข้อมูลค้างหรือทับค่าผิดพลาดเมื่ออินเทอร์เน็ตหลุด
- **🎨 ดีไซน์ Liquid Glass × Material Design 3:** หน้าจอโปร่งแสงโทนพาสเทลสบายตา พร้อมแถบเลื่อนแบบเต็มหน้าจอ

---

Version 1.1 ✨ การปรับปรุงหน้าตาและประสบการณ์ใช้งาน (UI / UX Improvements)
- **รองรับโหมดมืดอัตโนมัติ (Automatic Dark Mode):** เพิ่มระบบสลับธีมโหมดสว่างและโหมดมืดตามการตั้งค่าของอุปกรณ์โดยอัตโนมัติ (ผ่าน `values-night` และ `drawable-night`)
- **ป้ายหัวข้อกำกับหลอดโควตา (Quota Bar Labels):** เพิ่มหัวข้อกำกับด้านบนของหลอดแสดงผลแต่ละรอบอย่างชัดเจน ทั้ง **รอบ 5 ชั่วโมง** และ **ขีดจำกัดรายสัปดาห์**
- **การ์ดแสดงผลสะอาดตาขึ้น (Cleaner Quota Card):** ซ่อน/ตัดข้อความสรุปโควตาแบบซ้ำซ้อนด้านล่างหลอดแสดงผลออก ช่วยให้การ์ดดูคลีนและอ่านข้อมูลได้ง่ายขึ้น

---

## 📥 วิธีดาวน์โหลดและติดตั้ง

1. ไปที่เมนู **[Releases](../../releases)** ทางด้านขวามือ (หรือด้านล่างหากเปิดผ่านมือถือ)
2. ดาวน์โหลดไฟล์ **`GeminiUsage-v1.0.apk`** (หรือเวอร์ชันล่าสุด)
3. เปิดไฟล์ `.apk` ที่ดาวน์โหลดมา และกดอนุญาต **"ติดตั้งแอปที่ไม่รู้จัก (Install unknown apps)"**
4. เปิดแอป **Gemini Usage** กดปุ่มเข้าสู่ระบบด้วยบัญชี Google ที่ใช้งาน Gemini (ข้อมูลการล็อกอินทั้งหมดถูกเก็บไว้ในเครื่องของคุณ 100% ไม่มีการส่งออกไปยังเซิร์ฟเวอร์ภายนอก)

---

## ⚙️ การตั้งค่าแนะนำสำหรับ Xiaomi / HyperOS (เพื่อให้ Widget อัปเดตต่อเนื่อง)

เพื่อให้ระบบไม่ปิดการทำงานเบื้องหลังของแอป แนะนำให้ตั้งค่าดังนี้:
1. กดค้างที่ไอคอนแอป **Gemini Usage** → เลือก **ข้อมูลแอป (App info)**
2. เปิดสวิตช์ **เริ่มอัตโนมัติ (Autostart)**
3. เข้าเมนู **ประหยัดแบตเตอรี่ (Battery saver)** → เลือก **ไม่มีข้อจำกัด (No restrictions)**

---

## 🔒 ความปลอดภัยและความเป็นส่วนตัว

โปรเจกต์นี้เปิดเผยซอร์สโค้ดทั้งหมดแบบ Open Source แอปทำงานโดยดึงข้อมูลหน้าสถิติการใช้งานของตัวคุณเองผ่านระบบ WebView ภายในเครื่องเท่านั้น **ไม่มีการเก็บ รวบรวม หรือส่งต่อรหัสผ่านและคุกกี้ไปยังบุคคลที่สาม**

---
---

# 🌐 English Version

## 🌟 Features

- **📊 Dual-Quota Tracking:** Displays usage percentages and next reset timers for both **5-Hour Limit** and **Weekly Limit**.
- **🖼️ Two-Tone Home Screen Widget:** Place a clean widget right on your home screen to monitor your usage bars at a glance without opening the app.
- **📱 Xiaomi Rear Screen Support (Xiaomi 17 Pro Max, 18 Pro Max):** Automatically adapts layout margins and scaling when launched on the rear display via [Magic Rear](https://gitlab.com/896kb/ai-usage-companion/-/releases) (developed by **88896**).
- **🔄 Automatic Background Sync:** Refreshes your quota data automatically on schedule (every 2 hours) with built-in safeguards against stale or failed network fetches.
- **🎨 Liquid Glass × Material Design 3 UI:** Translucent pastel glassmorphism cards paired with smooth full-screen touch scrolling.

---

Version 1.1 ✨ UI / UX Improvements
- **Automatic Dark Mode:** Added automatic theme switching between Light and Dark mode following the device system setting (`values-night` and `drawable-night`).
- **Quota Bar Labels:** Added clear section headers above each progress bar for **5-Hour Limit** and **Weekly Limit**.
- **Cleaner Quota Card:** Removed duplicate raw quota summary lines below the progress bars to keep the card clean and easy to scan.

---

## 📥 Download & Installation

1. Go to the **[Releases](../../releases)** section on the right sidebar (or scroll down on mobile).
2. Download the latest **`GeminiUsage-v1.0.apk`** file.
3. Open the downloaded `.apk` file and allow **"Install unknown apps"** if prompted.
4. Launch **Gemini Usage** and sign in with your Google account used for Gemini. *(All login cookies and session data stay 100% locally on your device and are never transmitted to any third-party server.)*

---

## ⚙️ Recommended Setup for Xiaomi / HyperOS (Background Sync)

To prevent HyperOS from killing background updates for the Home Screen Widget:
1. Long-press the **Gemini Usage** app icon → tap **App info**.
2. Enable **Autostart**.
3. Go to **Battery saver** → select **No restrictions**.

---

## 🔒 Privacy & Security

This project is completely open source. The app fetches your own usage statistics locally via an on-device WebView session. **No passwords, cookies, or personal data are collected, stored externally, or shared with third parties.**

---

<div align="center">
  Made with 💡 & AI Collaboration by <b>punnagun094 </b>
</div>
