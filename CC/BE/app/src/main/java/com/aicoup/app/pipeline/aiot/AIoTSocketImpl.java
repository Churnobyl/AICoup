package com.aicoup.app.pipeline.aiot;

import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:application.yaml")
public class AIoTSocketImpl implements AIoTSocket {

    @Value("${aiot.url}")
    String url;

    @Value("${aiot.startApi}")
    private String aiotStartApi;

    @Value("${aiot.realApi}")
    String gameStatusApi;

    String mockupData = "[\n" +
            "    {\n" +
            "        \"left_card\": 1,\n" +
            "        \"right_card\": 2,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 1,\n" +
            "        \"right_card\": 2,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 5,\n" +
            "        \"right_card\": 4,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 1,\n" +
            "        \"right_card\": 2,\n" +
            "        \"extra_card\": []\n" +
            "    }\n" +
            "]";

    String aiotMockingData = "{\n" +
            "  \"user_card\": [\n" +
            "    {\n" +
            "      \"cards\": [\n" +
            "        {\n" +
            "          \"class_id\": 4,\n" +
            "          \"center\": [\n" +
            "            0.5138021111488342,\n" +
            "            0.16249999403953552\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            0.013802111148834229,\n" +
            "            -0.3375000059604645\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 4.753261348803143\n" +
            "        },\n" +
            "        {\n" +
            "          \"class_id\": 5,\n" +
            "          \"center\": [\n" +
            "            0.6020833253860474,\n" +
            "            0.17037037014961243\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            0.10208332538604736,\n" +
            "            -0.3296296298503876\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 5.012712705274567\n" +
            "        }\n" +
            "      ],\n" +
            "      \"left_card\": 1,\n" +
            "      \"right_card\": 3,\n" +
            "      \"extra_card\": [],\n" +
            "      \"center_vector\": [\n" +
            "        0.057942718267440796,\n" +
            "        -0.333564817905426\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"cards\": [\n" +
            "        {\n" +
            "          \"class_id\": 4,\n" +
            "          \"center\": [\n" +
            "            0.11953125149011612,\n" +
            "            0.5564814805984497\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            -0.3804687485098839,\n" +
            "            0.05648148059844971\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 2.994216640671357\n" +
            "        },\n" +
            "        {\n" +
            "          \"class_id\": 1,\n" +
            "          \"center\": [\n" +
            "            0.12968750298023224,\n" +
            "            0.4166666567325592\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            -0.37031249701976776,\n" +
            "            -0.0833333432674408\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 3.3629405903967347\n" +
            "        }\n" +
            "      ],\n" +
            "      \"left_card\": 4,\n" +
            "      \"right_card\": 1,\n" +
            "      \"extra_card\": [],\n" +
            "      \"center_vector\": [\n" +
            "        -0.3753906227648258,\n" +
            "        -0.013425931334495544\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"cards\": [\n" +
            "        {\n" +
            "          \"class_id\": 1,\n" +
            "          \"center\": [\n" +
            "            0.5575520992279053,\n" +
            "            0.8629629611968994\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            0.05755209922790527,\n" +
            "            0.3629629611968994\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 1.4135435752949872\n" +
            "        },\n" +
            "        {\n" +
            "          \"class_id\": 2,\n" +
            "          \"center\": [\n" +
            "            0.46796876192092896,\n" +
            "            0.8736110925674438\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            -0.032031238079071045,\n" +
            "            0.37361109256744385\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 1.656321362771176\n" +
            "        }\n" +
            "      ],\n" +
            "      \"left_card\": 1,\n" +
            "      \"right_card\": 2,\n" +
            "      \"extra_card\": [],\n" +
            "      \"center_vector\": [\n" +
            "        0.012760430574417114,\n" +
            "        0.36828702688217163\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"cards\": [\n" +
            "        {\n" +
            "          \"class_id\": 3,\n" +
            "          \"center\": [\n" +
            "            0.9078124761581421,\n" +
            "            0.4546296298503876\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            0.4078124761581421,\n" +
            "            -0.04537037014961243\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 6.172387908484293\n" +
            "        },\n" +
            "        {\n" +
            "          \"class_id\": 5,\n" +
            "          \"center\": [\n" +
            "            0.90625,\n" +
            "            0.6083333492279053\n" +
            "          ],\n" +
            "          \"vector\": [\n" +
            "            0.40625,\n" +
            "            0.10833334922790527\n" +
            "          ],\n" +
            "          \"cluster\": -1,\n" +
            "          \"angle\": 0.2606024282749238\n" +
            "        }\n" +
            "      ],\n" +
            "      \"left_card\": 3,\n" +
            "      \"right_card\": 5,\n" +
            "      \"extra_card\": [],\n" +
            "      \"center_vector\": [\n" +
            "        0.40703123807907104,\n" +
            "        0.03148148953914642\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"deck_card\": {\n" +
            "    \"cards\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"class\": 2\n" +
            "      }\n" +
            "    ],\n" +
            "    \"center_point\": [\n" +
            "      0.5302083492279053,\n" +
            "      0.5231481194496155\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    @Override
    public void gameStart() {
        WebClient webClient = WebClient.builder().build();
        Mono<String> resp = webClient
                .post()
                .uri(url + aiotStartApi)
                .retrieve()
                .bodyToMono(String.class);

        String resultString = resp.block();
        System.out.println("resultString = " + resultString);
    }

    @Override
    public List<MMResponse> getDataFromAIoTServer(String body) {
        WebClient webClient = WebClient.builder().build();
        int retryCount = 0;

        while (true) {
            try {
                Mono<String> resp = webClient
                        .post()
                        .uri(url + gameStatusApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class);

                String resultString = resp.block();
                //String resultString = aiotMockingData;
                return convertJsonToMMResponseList(resultString);
            } catch (Exception e) {
                retryCount++;
                System.out.println("에러 발생. 재시도 중... (시도 횟수: " + retryCount + ")");
                System.err.println("에러 내용: " + e.getMessage());
            }
        }
    }

    @Override
    public List<MMResponse> convertJsonToMMResponseList(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode userCardNode = rootNode.get("user_card");

        List<MMResponse> mmResponseList = new ArrayList<>();

        for (JsonNode cardGroup : userCardNode) {
            MMResponse mmResponse = new MMResponse();

            mmResponse.setLeft_card(cardGroup.get("left_card").asInt());
            mmResponse.setRight_card(cardGroup.get("right_card").asInt());

            JsonNode extraCardNode = cardGroup.get("extra_card");
            List<Integer> extraCards = new ArrayList<>();
            if (extraCardNode.isArray()) {
                for (JsonNode extraCard : extraCardNode) {
                    extraCards.add(extraCard.asInt());
                }
            }
            mmResponse.setExtra_card(extraCards);

            mmResponseList.add(mmResponse);
        }

        return mmResponseList;
    }
}
