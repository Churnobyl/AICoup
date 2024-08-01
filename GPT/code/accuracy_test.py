import json
import openai
from tqdm import tqdm
import pandas as pd
import tkinter as tk
from tkinter import ttk

# OpenAI API 키 설정 (사용자 API 키를 여기에 입력하세요)
openai.api_key = 'sk-proj-pg7hrz7BPkT9oKtLGZnsT3BlbkFJU6NSzvJnQwN63TAELQHV'

# jsonl 파일 경로
file_path = 'action_data/basic/action_data_basic_09.jsonl'

def read_jsonl(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()
        data = [json.loads(line) for line in lines]
    return data

def get_model_prediction(messages):
    response = openai.ChatCompletion.create(
        model="ft:gpt-3.5-turbo-0125:personal:action-basic:9o5RKWmy",  # 여기서 올바른 모델 ID로 변경
        messages=messages,
        max_tokens=150  # 필요에 따라 조정
    )
    return response.choices[0].message['content'].strip()

def parse_action_target(content):
    result = json.loads(content)
    return result.get("action", ""), result.get("target", "")

def evaluate_model(data):
    results = []
    total = 0
    correct = 0
    for item_index, item in enumerate(tqdm(data, desc="Evaluating inputs")):
        conversation = item['messages']
        user_content = next(msg['content'] for msg in conversation if msg['role'] == 'user')
        expected_content = next(msg['content'] for msg in conversation if msg['role'] == 'assistant')

        expected_action, expected_target = parse_action_target(expected_content)
        
        messages = [{"role": msg["role"], "content": msg["content"]} for msg in conversation if msg["role"] in ["system", "user"]]
        
        for call_index in tqdm(range(10), desc=f"Input {item_index + 1}/{len(data)} Calls", leave=True):  # 각 입력마다 10번 호출 진행률 표시
            predicted_content = get_model_prediction(messages)
            predicted_action, predicted_target = parse_action_target(predicted_content)
            is_match = (predicted_action == expected_action) and (predicted_target == expected_target)
            
            results.append({
                "Input Index": item_index + 1,
                "Call Index": call_index + 1,
                "Expected Action": expected_action,
                "Expected Target": expected_target,
                "Predicted Action": predicted_action,
                "Predicted Target": predicted_target,
                "Match": is_match
            })
            
            if is_match:
                correct += 1
            total += 1
    
    accuracy = correct / total if total > 0 else 0
    return results, accuracy

def display_results(results, accuracy):
    root = tk.Tk()
    root.title("Evaluation Results")

    cols = ["Input Index", "Call Index", "Expected Action", "Expected Target", 
            "Predicted Action", "Predicted Target", "Match"]
    
    tree = ttk.Treeview(root, columns=cols, show='headings')
    for col in cols:
        tree.heading(col, text=col)
        tree.column(col, anchor='center')

    for result in results:
        tree.insert("", "end", values=[result[col] for col in cols])
    
    tree.pack(expand=True, fill='both')
    
    accuracy_label = tk.Label(root, text=f'모델의 정확도: {accuracy * 100:.2f}%')
    accuracy_label.pack()
    
    root.mainloop()

def main():
    data = read_jsonl(file_path)
    results, accuracy = evaluate_model(data)
    display_results(results, accuracy)

if __name__ == "__main__":
    main()

