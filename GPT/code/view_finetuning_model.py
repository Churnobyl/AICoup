import openai

# OpenAI API 키 설정 (사용자 API 키를 여기에 입력하세요)
openai.api_key = ""


# 사용 가능한 모델 목록 확인
def list_models():
    models = openai.Model.list()
    for model in models["data"]:
        print(model["id"])


if __name__ == "__main__":
    list_models()
