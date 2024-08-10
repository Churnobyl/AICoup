import CardHolder from "@/components/game/CardHolder";
import useActionStore from "@/stores/actionStore";
import useGameStore from "@/stores/gameStore";
import { IconContext } from "react-icons";
import { PiCoinVerticalFill } from "react-icons/pi";
import "./Player.scss";

type Props = {
  playerNumber: number;
  playerId: string;
  className?: string;
};

export const Player = (props: Props) => {
  const store = useGameStore();
  const actionStore = useActionStore();
  const { playerNumber, className, playerId } = props;

  const setTarget = () => {
    if (actionStore.isClickable) {
      actionStore.setSelectedTarget(playerId);
      actionStore.setIsClickable();
      console.log("setTarget :", playerId);
    }
  };

  return (
    <div className={`player ${className}`} onClick={setTarget}>
      <span>{store.members[playerNumber].name}</span>
      <span>
        <IconContext.Provider value={{ color: "yellow", size: "24px" }}>
          <PiCoinVerticalFill />
        </IconContext.Provider>
        {store.members[playerNumber].coin}
      </span>
      <CardHolder
        key={playerNumber}
        playerNumber={playerNumber}
        className={`cardHolder`}
      />
    </div>
  );
};

export default Player;
