import GamePage from "@/pages/GamePage";
import GameStatusPage from "@/pages/GameStatusPage.tsx";
import ReactDOM from "react-dom/client";
import {
  Route,
  RouterProvider,
  createBrowserRouter,
  createRoutesFromElements,
} from "react-router-dom";
import "./index.css";

/**
 * 라우터
 */
const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/">
      <Route index element={<GameStatusPage />} />
      <Route path="game" element={<GamePage />} />
    </Route>
  )
);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <RouterProvider router={router} />
);
