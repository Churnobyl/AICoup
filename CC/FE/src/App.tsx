import GamePage from "@/pages/GamePage";
import GameStatusPage from "@/pages/GameStatusPage";
import MainPage from "@/pages/MainPage";
import {
  Route,
  RouterProvider,
  createBrowserRouter,
  createRoutesFromElements,
} from "react-router-dom";
import "./App.css";

const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<GameStatusPage />}>
      <Route index element={<MainPage />} />
      <Route path="game" element={<GamePage />} />
    </Route>
  )
);

function App() {
  return (
    <>
      <RouterProvider router={router} />
    </>
  );
}

export default App;
