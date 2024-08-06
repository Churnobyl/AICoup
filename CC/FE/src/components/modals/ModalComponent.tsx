import Modal from "react-modal";
import "./ModalComponent.scss";
import Button from "@/components/ui/button/button";

Modal.setAppElement("#root"); // Make sure to bind modal to your appElement

type ModalProps = {
  isOpen: boolean;
  onRequestClose: () => void;
  content: string;
  onSelect: (a: string) => void;
  options: string[];
};

const ModalComponent = ({
  isOpen,
  onRequestClose,
  content,
  onSelect,
  options,
}: ModalProps) => {
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
          {options.map((option: string, index: number) => (
            <Button
              key={index}
              size="medium"
              children={option}
              onClick={() => onSelect(option)}
            />
          ))}
        </div>
      </div>
    </Modal>
  );
};

export default ModalComponent;
