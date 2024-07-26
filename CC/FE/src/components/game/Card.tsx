import { useState } from "react";
import "./Card.scss";

type Props = {};

const Card = (props: Props) => {
  const [num, setNum] = useState(0);

  return <div className="cardItem card">{num}</div>;
};

export default Card;
