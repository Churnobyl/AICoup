import Deck from "@/components/game/Deck";
import Player from "@/components/game/Player";
import useGameStore from "@/stores/gameStore";
import "./Board.scss";

type Props = {
  className: string;
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const Board = (_props: Props) => {
  const store = useGameStore();

  return (
    <div className="board">
      {store.members.map((member, index) => (
        <Player
          key={index}
          playerNumber={index}
          playerId={member.id}
          className={`player ${index !== 0 ? "active" : ""}`}
        />
      ))}
      <div style={{ position: "absolute" }}>
        <Deck />
      </div>
      {/* <MessageBubble message="asdasd" position="left" /> */}
    </div>
  );
};

export default Board;
