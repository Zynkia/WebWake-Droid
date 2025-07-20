# WebWake Droid

**WebWake Droid** is a comprehensive Android application designed to wake up Windows computers over a local network (Wake-on-LAN). It features a powerful, built-in web server that provides a user-friendly interface for managing devices and settings, complete with a robust authentication system.

## ✨ Features

- **Embedded Web Server**: Runs a Ktor-based web server on your Android device, accessible on your local network (default port 8080).
- **Wake-on-LAN (WOL)**: Easily wake up registered devices by sending a "magic packet" through the web interface.
- **Robust Authentication**:
    - Secure login system with password hashing (BCrypt).
    - All features are protected and require login.
- **Dynamic Web Interface**:
    - Built with Tailwind CSS for a modern and responsive UI.
    - **Dashboard**: View all registered devices, initiate WOL, and enter edit mode to edit or delete devices.
    - **Settings Page**: A central hub for all configurations.
- **Comprehensive Settings Management**:
    - **User Management**: Change your username and password.
    - **Device Management**: Add, edit, or delete MAC addresses for WOL.
    - **Port Configuration**: Change the default HTTP service port.
    - **Public Network Access**:
        - **Cloudflare Tunnel**: Expose your service with a Cloudflare Tunnel token.
        - **Ngrok**: Use Ngrok for public access by providing an auth token and optional custom domain.
    - **SSL/TLS Support**: Secure your web server with HTTPS by providing your own SSL certificate and private key.
- **Self-Contained**: All required dependencies, including `cloudflared` and `ngrok`, are packaged within the application.

## 🚀 Getting Started

**Note**: This application requires Android 7.0 (API 24) or higher.

1.  **Download the latest APK** from the [**GitHub Releases**](https://github.com/Zynkia/WebWake-Droid/releases) page.
2.  **Install the APK** on your Android device.
3.  Once the app is running, it will start a background service. A persistent notification will indicate that the web server is active.
4.  Open a web browser on any device connected to the same local network and navigate to your Android device's IP address (e.g., `http://192.168.1.100:8080`).

## 🛠️ Building from Source

### Prerequisites

- JDK 17
- Android Studio
- An Android device (or emulator) running Android 7.0 (API 24) or higher.

### Building and Running

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Zynkia/WebWake-Droid.git
    ```
2.  **Open the project** in Android Studio.
3.  **Sync Gradle** to download all the required dependencies.
4.  **Build and run** the application on your Android device.

## 🖥️ Web Interface

### Login

- **Default Credentials**:
    - **Username**: `admin`
    - **Password**: `admin`
- It is highly recommended to change the default password immediately after your first login.

### Dashboard

The main page after logging in. It displays a list of all your registered devices. From here, you can:
- **Wake a device**: Click the "Wake" button next to any device.
- **Manage devices**: Click the "Edit" button to toggle edit mode, which allows you to edit or delete devices.

### Settings

The settings page is divided into several sections:

- **User Settings**: Change your login username and password.
- **Public Access**:
    - **Cloudflare Tunnel**: Enter your Cloudflare Tunnel token to make your service accessible from the internet.
    - **Ngrok**: Configure Ngrok with your auth token and an optional custom domain.
- **SSL Settings**: Paste your SSL certificate and private key to enable HTTPS. The service must be restarted for changes to take effect.

## 🛠️ Technical Details

- **Backend**: Kotlin, Ktor (for the web server)
- **Frontend**: HTML, Tailwind CSS (via CDN)
- **Authentication**: Ktor Auth (Form-based and Sessions), jBCrypt
- **Persistence**: Android SharedPreferences with Kotlinx Serialization for object serialization.

## 📜 License & Trademarks

This project is licensed under the **GNU Affero General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

For information about trademarks, please see the [TRADEMARKS.md](TRADEMARKS.md) file.

---

This project aims to provide a powerful and flexible remote management tool, all from the convenience of your Android device.
