import Button from "@/components/ui/button/Button";
import { Link } from "react-router-dom";

const MainPage = () => {
  return (
    <>
      <Link to={"/game"}>
        <Button
          children={<span>게임 시작</span>}
          size="large"
          onClick={() => {}}
        />
      </Link>
    </>
  );
};

export default MainPage;
