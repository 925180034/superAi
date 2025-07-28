# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a Vue 3 frontend application for "Super AI" with Chinese UI. The main application code is located in the `super-ai-frontend/` directory.

Key directories:
- `src/pages/` - Main application pages (Home, LoveChat, ManusChat)
- `src/components/` - Reusable Vue components including Chat.vue and AIAvatar.vue
- `src/router/` - Vue Router configuration with lazy-loaded routes
- `src/assets/` - Static assets and CSS

## Development Commands

All commands should be run from the `super-ai-frontend/` directory:

```bash
# Install dependencies
npm install

# Start development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Architecture

- **Framework**: Vue 3 with Composition API
- **Build Tool**: Vite with Vue plugin
- **Routing**: Vue Router with lazy-loaded components
- **HTTP Client**: Axios for API requests
- **Backend Integration**: API proxy configured to `http://localhost:8123` during development

## API Configuration

- Development proxy: `/api` routes to `http://localhost:8123` (vite.config.js:19-23)
- Production proxy: nginx proxies `/api` to `https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api/`

## Deployment

The application is containerized using Docker:
- Multi-stage build with Node.js 20 Alpine for building
- Nginx stable Alpine for serving static files
- Custom nginx.conf handles SPA routing and API proxying
- Exposes port 80

## Component Structure

Main application features:
- Multi-page SPA with navigation between Home, AI Love Master (/love), and AI Super Agent (/manus)
- Chat components for AI interactions
- Responsive design with mobile support
- Chinese language interface