# Image2PDF
Image2PDF is a native Android application designed to convert images into PDF files efficiently. You can source images directly from your device's camera or select them from the gallery. The app provides a straightforward interface for managing your created documents, including viewing, sharing, and deleting them.

A standout feature is the **Linux Integration**, which allows users to generate PDFs remotely. By activating a local server within the app, you can send images from any computer on the same network using a simple `curl` command and receive a PDF in return.

## Key Features

*   **Create PDFs from Multiple Sources**:
    *   Capture images using the in-app camera.
    *   Select multiple images from your device's gallery.
*   **Camera Controls**:
    *   Toggle flash on and off.
    *   Switch between maximum quality and minimum latency capture modes.
*   **PDF Management**:
    *   View all created PDFs in a list, sorted by frequency of use.
    *   Open, share, and delete PDFs directly from the app.
    *   Search for specific PDFs by name.
*   **Linux Integration for Remote PDF Creation**:
    *   Activate a lightweight HTTP server on your Android device.
    *   Send images from a computer on the same network via a `curl` command.
    *   The app processes the images and sends the generated PDF back as the HTTP response.

## How to Use

### Creating PDFs on Your Device

1.  **From the Camera**:
    *   Navigate to the camera screen from the main menu.
    *   Use the capture button to take photos.
    *   Once you have captured all the required images, tap the save icon.
    *   Enter a name for your PDF to complete the process.
2.  **From the Gallery**:
    *   From the main screen, tap the gallery icon.
    *   Select one or more images from your device's gallery.
    *   After selection, you will be prompted to enter a name for the new PDF.

### Using the Linux Integration

This feature allows you to create a PDF by sending images from your computer (e.g., a Linux PC) to your phone.

1.  **Start the Server**:
    *   Open the Image2PDF app and navigate to the Linux Integration screen (penguin icon).
    *   Activate the server using the toggle switch. The app will display the `curl` command you need to use, which includes your phone's local IP address.

2.  **Send Images from Your Computer**:
    *   Open a terminal on a computer connected to the same Wi-Fi network as your phone.
    *   Use the `curl` command provided by the app to send your images. You can include multiple files.

    **Example Command:**
    ```bash
    curl -X POST -F "file1=@/path/to/your/image1.jpg" -F "file2=@/path/to/your/image2.png" --output MyRemotePDF.pdf http://<YOUR_PHONE_IP>:8080
    ```
    *   Replace `/path/to/your/image.jpg` with the actual paths to your image files.
    *   The generated `MyRemotePDF.pdf` will be saved on your computer.

## Installation

You can install the application on your Android device in two ways:

1.  **Directly from the APK**:
    *   Download the `app-release.apk` file from the [`app/release/`](https://github.com/Kiriaevi/image2pdf/tree/main/app/release) directory of this repository.
    *   Transfer the APK to your Android device and open it to install. You may need to enable installation from unknown sources.
2.  **Build from Source**:
    *   Clone the repository: `git clone https://github.com/Kiriaevi/image2pdf.git`
    *   Open the project in Android Studio.
    *   Build the project and run it on an emulator or a physical device.

## Core Technologies

*   **PDF Generation**: [iText7](https://github.com/itext/itext7)
*   **Camera Functionality**: [Android CameraX](https://developer.android.com/training/camerax)
*   **HTTP Server**: [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)
*   **Language**: Kotlin


<img width="300px" height="600px" alt="immagine" src="https://github.com/user-attachments/assets/5511390c-4e3c-4eaf-bf2e-17443e186718" />
<img width="300px" height="600px" alt="immagine" src="https://github.com/user-attachments/assets/07f64b57-2d90-4941-a77b-61f920fc4e0b" />

