// app.ts

// Define the structure for the frame data we might receive (even if static for now)
interface FrameStats {
    fps: number;
    resolution: string;
    status: string;
    imageUrl: string;
}

const loadViewer = () => {
    // 1. Get DOM elements
    const imageElement = document.getElementById('processed-image') as HTMLImageElement;
    const fpsElement = document.getElementById('fps');
    const statusElement = document.getElementById('status');
    const resElement = document.getElementById('res');

    // 2. Define static sample data (simulating data received from Android)
    const sampleStats: FrameStats = {
        fps: Math.floor(Math.random() * 5) + 12, // Simulate a random FPS between 12-17
        resolution: "640x480",
        status: "Processing Mock Data",
        // The image file path relative to index.html
        imageUrl: "./src/sample_frame.png"
    };

    // 3. Update Image Source
    if (imageElement) {
        imageElement.src = sampleStats.imageUrl;
    }

    // 4. Update Stats Overlay
    if (fpsElement) {
        fpsElement.textContent = sampleStats.fps.toFixed(1);
    }
    if (statusElement) {
        statusElement.textContent = `Status: ${sampleStats.status}`;
    }
    if (resElement) {
        resElement.textContent = sampleStats.resolution;
    }

    console.log("Web Viewer Initialized with static frame.");
};

// Run the initialization function once the DOM is fully loaded
document.addEventListener('DOMContentLoaded', loadViewer);