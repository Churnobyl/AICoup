package com.aicoup.app.websocket.model.repository;

import com.aicoup.app.websocket.model.dto.RoomDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RoomRepository {

    private Map<String, RoomDto> roomDtoMap;

    @PostConstruct
    private void init() {
        roomDtoMap = new LinkedHashMap<>();
    }

    public List<RoomDto> findAllRooms() {
        ArrayList<RoomDto> result = new ArrayList<>(roomDtoMap.values());
        Collections.reverse(result);
        return result;
    }

    public RoomDto findRoomById(String id) {
        return roomDtoMap.get(id);
    }

    public RoomDto createRoomDto(String name) {
        RoomDto room = RoomDto.create(name);
        roomDtoMap.put(room.getRoomId(), room);

        return room;
    }
}
