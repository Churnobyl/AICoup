import Deck from "@/components/game/Deck";
import Player from "@/components/game/Player";
import useGameStore from "@/stores/gameStore";
import "./Board.scss";

type Props = {
  className: string;
  isClickable: boolean;
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const Board = (_props: Props) => {
  const store = useGameStore();

  return (
    <div className="board">
      {store.members.map((_member, index) => (
        <Player
          key={index}
          playerNumber={index}
          className={`player ${index !== 0 ? "active" : ""}`}
          isClickable={index !== 0}
        />
      ))}
      <div style={{ position: "absolute" }}>
        <Deck />
      </div>
    </div>
  );
};

export default Board;
