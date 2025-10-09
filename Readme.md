Real-Time Canny Edge Detection (Android NDK)
This project demonstrates a high-performance real-time computer vision pipeline on Android by integrating the CameraX API with native C++ (NDK), OpenCV, and OpenGL ES 2.0.

🌟 Implemented Features
Category

Features Implemented (Android/Kotlin)

Features Implemented (Native C++/OpenCV)

Video Processing

Real-time frame capture using CameraX.

Canny Edge Detection on every live frame.

Performance

Efficient frame transfer using raw YUV buffers.

High-speed processing via native C++ (NDK).

Rendering

High-performance rendering using OpenGL ES 2.0.

Direct data upload from OpenCV cv::Mat to an OpenGL texture (glTexImage2D).

Usability

Automated Camera Permission Handling.

Corrected video orientation and mirroring for proper display.

📸 Screenshots and Visual Output
Since this is a high-performance application, the visual quality and responsiveness are key features. When documenting your project, you should include screenshots or, ideally, a short GIF to demonstrate the real-time effect.

Visual Element Description
Live Canny Edges (GIF) A short GIF showing the device moving and the edges changing dynamically, demonstrating zero-lag real-time processing.
Camera Input A static image (or frame) of the original scene (e.g., a room or an object).
Processed Output A corresponding static image (like your output2.jpeg) showing the same scene after Canny Edge Detection has been applied (black background with white lines).

⚙️ Setup Instructions (NDK & OpenCV)
This project requires native development tools to compile the C++ code and link the OpenCV library.

1. Android NDK and CMake
   Install NDK & CMake: In Android Studio, go to Settings (or Preferences) → SDK Manager → SDK Tools. Install the following components:

NDK (Side by Side)

CMake

Verify Path: Ensure the NDK path is correctly configured in your project's local.properties file.

2. OpenCV Dependencies
   This step ensures your C++ code can access the OpenCV library functions (like cv::Canny and cv::cvtColor).

Obtain OpenCV Library: You must have the prebuilt OpenCV Android SDK available in your project structure (or linked via Gradle).

Configure CMake: Verify that your CMakeLists.txt file correctly uses find_package(OpenCV REQUIRED) and links the necessary libraries (e.g., opencv_core, opencv_imgproc) using target_link_libraries in the native build configuration.

🏛️ Architecture: JNI and Frame Flow
This architecture is designed for maximum speed by utilizing the GPU for rendering and native C++ for processing.

Frame Flow (The 4-Step Cycle)
Capture (Kotlin/CameraX): The Kotlin layer uses CameraX's ImageAnalysis to capture live frames and obtain the raw YUV ByteBuffer data.

Dispatch (JNI): The Kotlin code calls a native C++ function (nativeProcessFrame) via the JNI (Java Native Interface) bridge. This transfers the raw frame buffers and the GPU's OpenGL Texture ID to the native side.

Process (C++/OpenCV):

C++ creates a cv::Mat wrapper for the Y-plane (grayscale data).

OpenCV runs the Canny Edge Detection algorithm.

The result is converted to RGBA.

The processed image data is uploaded directly to the GPU texture using glTexImage2D.

Render (OpenGL): The MyGLRenderer uses OpenGL ES to continuously draw the updated texture onto the screen, resulting in the real-time edge effect.

Note on TypeScript/Web
The main application logic runs entirely on Android/Kotlin/C++.

The separate Web/HTML component is purely a static viewer used to display a pre-processed output image from the main Android project; it does not contain the live Canny processing logic (C++/JNI).
