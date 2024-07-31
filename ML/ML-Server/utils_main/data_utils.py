from core import *

''' JSON 포멧
[
    {
        "left_card": 4,
        "right_card": 3,
        "extra_card": []
    },
    {
        "left_card": 1,
        "right_card": 2,
        "extra_card": []
    },
    {
        'left_card': 5,
        'right_card': 4,
        'extra_card': []
    },
    {
        'left_card': 1,
        'right_card': 2,
        'extra_card': []
    }
]
'''

# -----------------------------------------------------------
### 데이터 포멧 변환
def convert_to_dict(all_files_data):
    results = [] 

    # 이미지 3장의 객체 탐지 결과 순차적으로 탐색
    count = 0
    for file_data in all_files_data:
        count += 1
        file_results = []
        for line in file_data:
            class_id, x, y, w, h, conf = map(float, line)
            file_results.append({
                "class_id": int(class_id),
                "x": x,
                "y": y,
                "width": w,
                "height": h,
                "confidence": conf
            })
        results.append({
            "image": count,
            "detections": file_results
        })
    
    return results

# -----------------------------------------------------------
### 이미지 파일 불러오기
async def load_image(image_path):
    try:
        with open(image_path, "rb") as image_file:
            print(image_path, "이미지 파일 읽기")
            return image_file.read()
        
    except FileNotFoundError:
        print(f"파일을 찾을 수 없습니다: {image_path}")
        return None
    

# -----------------------------------------------------------
### 텍스트 파일 불러오기 및 변환
def convert_txt_file():
    print("텍스트 파일 변환 시작")
    
    results = []

    for txt in TXT_FILES:
        txt_path = DET_FOLDER/'exp/labels' / txt
        txt_list = []

        try:
            if txt_path.is_file():
                with open(txt_path, 'r') as f: # read-only
                    print(txt_path, "텍스트 파일 읽어오기")
                    for line in f:
                        txt_list.append(line.strip().split())
                results.append(txt_list)
            else:
                print(f"File {txt_path} does not exist.")
                results.append(txt_list)  # Append empty list if file does not exist

        except Exception as e:
            print(f"Error reading file {txt_path}: {e}")
            results.append(txt_list)  # Append empty list if there is an error

    print("텍스트 파일 변환 종료")
    return results    