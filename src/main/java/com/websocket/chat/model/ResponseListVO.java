package com.websocket.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Name: ResponseListVO
 * Description: 응답 리스트 VO
 * Creation Date: 2024.03.09
 * */

@Getter
@Setter
@AllArgsConstructor
public class ResponseListVO<T> {

    /** 응답 리스트 **/
    private List<T> list;

    /** 응답 리스트 전체 건수 **/
    private Long cnt;

}
