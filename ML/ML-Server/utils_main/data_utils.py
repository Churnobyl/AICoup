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
# 객체 탐지 결과 txt 반환
# -----------------------------------------------------------
### 데이터 포멧 1차 변환
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
### 로그 큐 변환
def convert_queue():
    print("로그 큐 변환 시작")
    
    count = len(DET_LOGS)
    if count == 0:
        print("로그 없음")
        return "이전 객체 탐지 내역 없음"
    
    results = []
    seq = ""
    for log in DET_LOGS:  
        if log:
            res = convert_to_dict(log)
        else:
            res = []
        
        count -= 1
        match count:
            case 0:
                seq = "최신"
            case 1:
                seq = "이전"
            case 2:
                seq = "가장 오래 된"
        
        results.append({
            "time": f"{seq} 탐지 결과",
            "log": res
        })

    print("로그 큐 변환 종료")
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
    

