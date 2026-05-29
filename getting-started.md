# Getting Started

Welcome to OmniGate Commerce! This guide will help you set up and run the application.

## Prerequisites

- [Android Studio](https://developer.android.com/studio)
- A Gemini API key

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/N736-1/Affiliate-Ai-Drop-shipping-app.git
   cd Affiliate-Ai-Drop-shipping-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **Open** and choose the project directory
   - Allow Android Studio to fix any incompatibilities as it imports the project

3. **Configure API Key**
   - Create a file named `.env` in the project directory
   - Set `GEMINI_API_KEY` in that file to your Gemini API key
   - See `.env.example` for reference

4. **Build Configuration**
   - Remove this line from the app's `build.gradle.kts` file:
     ```kotlin
     signingConfig = signingConfigs.getByName("debugConfig")
     ```

5. **Run the Application**
   - Run the app on an emulator or physical device
   - The app will be built and deployed to your device

## Features Overview

### AI Agent Swarm
The platform uses multiple AI agents working together to manage different aspects of your e-commerce business.

### Dropshipping Management
Automate product sourcing, inventory management, and order fulfillment.

### Affiliate Marketing
Track affiliate links, commissions, and campaign performance.

### Sales Telemetry
Get real-time insights into your sales data, customer behavior, and business metrics.

## Next Steps

- Explore the [API Documentation](/api)
- View the source code on [GitHub](https://github.com/N736-1/Affiliate-Ai-Drop-shipping-app)
- Check out the [AI Studio App](https://ai.studio/apps/451e8789-312a-45b0-8366-eabe18339690)
