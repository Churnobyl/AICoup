import useCardInfoStore from "@/stores/cardInfoStore";
import useCardSelectStore from "@/stores/cardSelectStore";
import "./Card.scss";

type Props = {
  cardNumber: number;
  player: boolean;
  playerCardIdForSelect: number;
};

const Card = (props: Props) => {
  const { cardNumber, player, playerCardIdForSelect } = props;
  const store = useCardInfoStore();
  const cardSelectStore = useCardSelectStore();

  const setPlayerCard = () => {
    if (cardSelectStore.isPlayerCardClickable) {
      cardSelectStore.setIsPlayerCardClickable();
      cardSelectStore.setSelectedPlayerCard(playerCardIdForSelect);
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
        cardSelectStore.isPlayerCardClickable && player ? "clickable" : ""
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
