import CardHolder from "@/components/game/CardHolder";
import useGameStore from "@/stores/gameStore";

type Props = {
  playerNumber: number;
  className?: string;
  isClickable: boolean;
};

export const Player = (props: Props) => {
  const store = useGameStore();
  const { playerNumber, className, isClickable } = props;

  const handleClick = () => {
    if (isClickable) {
      console.log("asdasds");
    }
  };

  return (
    <div className={`player ${className}`}>
      <span>{store.members[playerNumber].name}</span>
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
