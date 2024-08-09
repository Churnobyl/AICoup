import Card from "@/components/game/Card";
import "./CardHolder.scss";

type Props = {};

const CardHolder = (props: Props) => {
  return (
    <div className="cardHolder">
      <Card />
      <Card />
    </div>
  );
};

export default CardHolder;
