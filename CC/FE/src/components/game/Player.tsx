import CardHolder from "@/components/game/CardHolder";
import useGameStore from "@/stores/gameStore";
import { IconContext } from "react-icons";
import { FaBitcoin } from "react-icons/fa";
import { PiCoinVerticalDuotone, PiCoinVerticalFill } from "react-icons/pi";

type Props = {
  playerNumber: number;
  className?: string;
  isClickable: boolean;
};

export const Player = (props: Props) => {
  const store = useGameStore();
  const {
    playerNumber,
    className,
    //isClickable
  } = props;

  // const handleClick = () => {
  //   if (isClickable) {
  //     console.log("asdasds");
  //   }
  // };

  return (
    <div className={`player ${className}`}>
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
        isClickable={playerNumber !== 0}
      />
    </div>
  );
};

export default Player;
