import { ActionType } from "@/types/ActionType";
import Modal from "react-modal";
import "./ModalComponent.scss";
import Button from "@/components/ui/button/Button";

Modal.setAppElement("#root");

type ModalProps = {
  isOpen: boolean;
  onRequestClose: () => void;
  content: string;
  onSelect: (a: number) => void;
  options: ActionType;
};

const ModalComponent = ({
  isOpen,
  onRequestClose,
  content,
  onSelect,
  options,
}: ModalProps) => {
  const actionArray = Object.entries(options);
  actionArray.sort((a, b) => a[1] - b[1]);

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={onRequestClose}
      contentLabel="Game Modal"
      className="modal"
      overlayClassName="overlay"
    >
      <div className="modal-content">
        <p>{content}</p>
        <div className="modal-actions">
          {actionArray.map((value) => (
            <Button
              key={value[1]}
              children={value[0]}
              size="large"
              onClick={() => onSelect(value[1])}
            />
          ))}
        </div>
      </div>
    </Modal>
  );
};

export default ModalComponent;
