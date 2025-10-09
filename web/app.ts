/**
 * Real-Time Edge Detection Web Viewer - TypeScript Logic
 * * This file demonstrates setting static frame statistics using TypeScript 
 * and proper DOM manipulation for a web viewer.
 */

// Define an interface for the static frame statistics
interface FrameStats {
    resolution: string;
    fps: number;
}

// 1. Get DOM elements and ensure they are present (type assertion as HTMLElement)
const resElement = document.getElementById('res') as HTMLSpanElement | null;
const fpsElement = document.getElementById('fps') as HTMLSpanElement | null;
const imageElement = document.getElementById('processed-image') as HTMLImageElement | null;

// 2. Define the static data structure using the interface
const staticStats: FrameStats = {
    resolution: '640 x 480', // Standard camera resolution from the Android app
    fps: 15.2
};

// 3. Function to update the DOM elements
function updateStats(stats: FrameStats): void {
    if (resElement) {
        // TypeScript ensures resElement is not null here
        resElement.textContent = stats.resolution;
    } else {
        console.error("Resolution element (id='res') not found.");
    }

    if (fpsElement) {
        // TypeScript ensures fpsElement is not null here
        fpsElement.textContent = stats.fps.toFixed(1);
    } else {
        console.error("FPS element (id='fps') not found.");
    }

    if (imageElement) {
        // Add a visual confirmation that TS code ran successfully
        imageElement.style.outline = "4px solid #4ade80"; 
        imageElement.style.transition = "outline 0.5s";
    }
}

// 4. Run the update function once the script is loaded
document.addEventListener('DOMContentLoaded', () => {
    updateStats(staticStats);
    console.log("Web Viewer initialized successfully with TypeScript.");
});

// For demonstration purposes, you could add an interactive element:
console.log(`Frame Data: ${staticStats.resolution} at ${staticStats.fps} FPS.`);
