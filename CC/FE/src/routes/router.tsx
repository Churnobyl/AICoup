import { createBrowserRouter } from "react-router-dom";
import GamePage from "../pages/GamePage";
import GameStatusPage from "../pages/GameStatusPage";

const router = createBrowserRouter([
  {
    path: "/",
    element: <GameStatusPage />,
  },
  {
    path: "game",
    element: <GamePage />,
  },
]);

export default router;
