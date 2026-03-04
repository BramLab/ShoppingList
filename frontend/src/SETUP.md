# ShoppingList Frontend — Setup

## 1. Create the Vite project
```bash
npm create vite@latest frontend -- --template react
cd frontend
```

## 2. Install dependencies
```bash
npm install
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install react-router-dom
```

## 3. Copy files
Replace / merge the files from this scaffold into your `frontend/` folder.

## 4. Run
Make sure your Spring Boot backend is running on port 8080, then:
```bash
npm run dev
```

Frontend runs on http://localhost:5173 (already allowed by your CorsConfig).

## Seed credentials (from ConfigViaService)
- Admin:  `user01` / `hashed01?`
- Normal: `user02` / `hashed02?`
