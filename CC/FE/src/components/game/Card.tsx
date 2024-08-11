import useActionStore from "@/stores/actionStore";
import "./Card.scss";
import useCardInfoStore from "@/stores/cardInfoStore";

type Props = {
  cardNumber: number;
  player: boolean;
  playerCardIdForSelect: number;
};

const Card = (props: Props) => {
  const { cardNumber, player, playerCardIdForSelect } = props;
  const store = useCardInfoStore();
  const actionStore = useActionStore();

  const setPlayerCard = () => {
    if (actionStore.isPlayerCardClickable) {
      actionStore.setSelectedPlayerCard(playerCardIdForSelect);
      actionStore.setIsPlayerCardClickable();
    }
  };

  return (
    <div
      className={`cardItem card card-${
        player
          ? cardNumber < 0
            ? -cardNumber
            : cardNumber
          : cardNumber < 0
          ? -cardNumber
          : 0
      } ${cardNumber < 0 ? "dead" : ""} ${
        actionStore.isPlayerCardClickable && player ? "clickable" : ""
      }`}
      onClick={setPlayerCard}
    >
      {player
        ? store.cardname[cardNumber > 0 ? cardNumber : -cardNumber]
        : cardNumber < 0
        ? store.cardname[-cardNumber]
        : ""}
    </div>
  );
};

export default Card;
