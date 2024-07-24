import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { statusCheckApi } from "../apis/statusCheckApi";
import MainPage from "./MainPage";

const GameStatusPage = () => {
  const [gameStarted, setGameStarted] = useState<boolean | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    statusCheckApi().then((response) => {
      if (response) {
        navigate("/game");
      }

      setGameStarted(false);
    });
  }, [navigate]);

  if (gameStarted === null) {
    return <div>Loading...</div>;
  }

  return <MainPage />;
};

export default GameStatusPage;
