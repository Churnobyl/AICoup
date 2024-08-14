import "./SignBoard.scss";
import { Callout } from "@blueprintjs/core";

const SignBoard = ({ title }: { title: string }) => {
  return (
    <Callout
      {...{ compact: true, icon: "selection", intent: "primary" }}
      title={title}
      className={"signboard"}
    />
  );
};

export default SignBoard;
