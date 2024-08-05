import Card from "@/components/game/Card";
import "./CardHolder.scss";
import useGameStore from "@/stores/gameStore";
import classNames from "classnames";

type Props = {
  playerNumber: number;
  className?: string;
  isClickable: boolean;
};

const CardHolder = (props: Props) => {
  const store = useGameStore();
  const { playerNumber, className, isClickable } = props;

  const handleClick = () => {
    if (isClickable) {
      console.log("asdasds");
    }
  };

  return (
    <div
      className={classNames(className, {
        clickable: isClickable,
      })}
      onClick={handleClick}
    >
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].leftCard}
      />
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].rightCard}
      />
    </div>
  );
};

export default CardHolder;
