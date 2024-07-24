import { Link, useNavigate } from "react-router-dom";
import Button from "../components/ui/button/Button";

const MainPage = () => {
  const navigate = useNavigate();

  const onClick = () => {
    navigate("/game");
  };

  return (
    <>
      <Link to={"/game"}>게임 시작</Link>
    </>
  );
};

export default MainPage;
