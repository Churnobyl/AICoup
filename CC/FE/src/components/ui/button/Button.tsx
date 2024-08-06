import classNames from "classnames";
import "./button.scss";

type ButtonType = {
  children: React.ReactNode;
  size: "small" | "medium" | "large";
  onClick: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
};

function Button({ children, size = "medium", onClick }: ButtonType) {
  return (
    <button className={classNames("button", size)} onClick={onClick}>
      {children}
    </button>
  );
}

export default Button;
