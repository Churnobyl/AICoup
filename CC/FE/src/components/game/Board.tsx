import CardHolder from "@/components/game/CardHolder";
import "./Board.scss";

type Props = {};

const Board = (props: Props) => {
  return (
    <div className="board">
      <CardHolder />
      <CardHolder />
      <CardHolder />
      <CardHolder />
    </div>
  );
};

export default Board;
