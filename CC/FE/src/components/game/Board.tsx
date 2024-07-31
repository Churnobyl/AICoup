import CardHolder from "@/components/game/CardHolder";
import "./Board.scss";
import useGameStore from "@/stores/gameStore";
import Deck from "@/components/game/Deck";

type Props = {
  className: string;
};

const Board = (props: Props) => {
  const store = useGameStore();

  return (
    <div className="board">
      {store.members.map((member, index) => (
        <CardHolder key={index} playerNumber={index} className="card-holder" />
      ))}
      <div style={{ position: "absolute" }}>
        <Deck />
      </div>
    </div>
  );
};

export default Board;
