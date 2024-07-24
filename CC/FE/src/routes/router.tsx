import { createBrowserRouter } from "react-router-dom";
import GamePage from "./GamePage";
import GameStatusPage from "./GameStatusPage";

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
