import "./Button.scss";

type Props = {
  text: string;
};

function Button({ text, onClick }: Props) {
  console.log(onClick);
  return <button>{text}</button>;
}

export default Button;
