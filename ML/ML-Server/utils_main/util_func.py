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

### JSON 데이터 변환
def makeDetectionsJsonResult(detections):
    #JSON으로 전달할 객체 탐지 결과를 담을 리스트
    results = [] 

    # 이미지 3장의 객체 탐지 결과 순차적으로 탐색
    count = 0
    for img_det in detections:
        count += 1
        det_results = []
        for det in img_det:
            class_id, x, y, w, h, conf = map(float, det)
            det_results.append({
                "class_id": int(class_id),
                "x": x,
                "y": y,
                "width": w,
                "height": h,
                "confidence": conf
            })
        results.append({
            "image": count,
            "detections": det_results
        })
    
    return results