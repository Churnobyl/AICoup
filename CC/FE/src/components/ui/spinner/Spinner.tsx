import { PuffLoader } from "react-spinners";
import "./Spinner.scss";

const Spinner = ({ text, isLoading }: { text: string; isLoading: boolean }) => {
  if (!isLoading) {
    return null;
  }

  return (
    <div className="spinner-container">
      <span>{text}</span>
      <PuffLoader loading={isLoading} />
    </div>
  );
};

export default Spinner;
