type BubbleType = {
  text: string;
};

export default function Bubble({ text }: BubbleType) {
  return <div className={"bubble"}>{text}</div>;
}
